package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.MediaItemRepository
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItem
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import androidx.core.graphics.scale
import kotlinx.coroutines.flow.combine
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toDto
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.toEntity
import java.util.UUID

@HiltViewModel
class MediaItemViewModel @Inject constructor(
    private val mediaItemRepository: MediaItemRepository
) : ViewModel() {

    var listState by mutableStateOf(ListState())
        private set

    var alertDialogUiState by mutableStateOf(AlertDialogUiState())
        private set

    var minimalDialogUiState by mutableStateOf(MinimalDialogUiState())
        private set

    var deleteDialogUiState by mutableStateOf(DeleteDialogUiState())
        private set

    var bottomSheetUiState by mutableStateOf(BottomSheetUiState())
        private set


    fun changeListState(enum: ListFilter): ListFilter {
       when(enum) {
           ListFilter.REMOTE -> {
               ListState(
                   remoteButton = true
               )
               return ListFilter.REMOTE
           }
           ListFilter.ALL ->  {
               ListState(
                   allButton = true
               )
               return ListFilter.ALL
           }
           else -> {
               ListState(
                   localButton = true
               )
               return ListFilter.LOCAL
           }
       }
    }


    val localMediaItemListUiState: StateFlow<LocalMediaItemListUiState> =
        mediaItemRepository.getAllMediaItemsStream().map { LocalMediaItemListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = LocalMediaItemListUiState()
            )

    val remoteItemListUiState: StateFlow<RemoteItemListUiState> = combine(
        mediaItemRepository.getRemoteItems(),
        localMediaItemListUiState
    ) { mediaItemDTOs, localState ->
        val maxLocalId = localState.mediaItems.maxOfOrNull { it.id } ?: 0L

        val entities = mediaItemDTOs.mapIndexed { index, dto ->
            val entity = dto.toEntity().copy(
                id = maxLocalId + index + 1
            )

            // Debug: Prüfe gemappte Entity
            Log.d("MappedEntity", "id=${entity.id}, remoteId='${entity.remoteId}', title='${entity.title}'")

            entity
        }

        RemoteItemListUiState(mediaItems = entities)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = RemoteItemListUiState()
    )

    val allMediaItemsUiState: StateFlow<AllMediaItemsUiState> = combine(
        localMediaItemListUiState,
        remoteItemListUiState
    ) { localState, remoteState ->
        AllMediaItemsUiState(
            mediaItems = localState.mediaItems + remoteState.mediaItems
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AllMediaItemsUiState()
    )


    fun changeAlertDialogUiState(dialogState: Boolean) {
        alertDialogUiState =
            AlertDialogUiState(isDialogVisible = !dialogState)
    }

    fun changeBottomSheetUiState(sheetState: Boolean) {
        bottomSheetUiState =
            BottomSheetUiState(isBottomSheetVisible = !sheetState)
    }

    fun changeMinimalDialogUiState(
        dialogUiState: Boolean,
        mediaItem: MediaItemDetails,
    ) {
        minimalDialogUiState =
            MinimalDialogUiState(
                mediaItem = mediaItem,
                isMinimalDialogVisible = !dialogUiState
            )
    }

    fun changeDeleteDialogUiState(
        dialogUiState: Boolean,
        mediaItem: MediaItemDetails,
    ) {
        deleteDialogUiState =
            DeleteDialogUiState(
                mediaItem = mediaItem,
                isDeleteDialogVisible = !dialogUiState
            )
    }

    fun transferMediaItemToBottomUiState(mediaItem: MediaItemDetails) {
        bottomSheetUiState =
            BottomSheetUiState(
                mediaItemDetails = mediaItem,
                isBottomSheetVisible = true,
            )
    }

    fun updateBottomSheetUiState(mediaItem: MediaItemDetails) {
        bottomSheetUiState = BottomSheetUiState(
            mediaItemDetails = mediaItem,
            isBottomSheetVisible = true,
            isEntryValid = validateInput(mediaItem),
            isLocalButtonChosen = bottomSheetUiState.isLocalButtonChosen
        )
    }

    fun changeLocalRemoteButton(bool: Boolean) {
        bottomSheetUiState = BottomSheetUiState(
            mediaItemDetails = bottomSheetUiState.mediaItemDetails,
            isBottomSheetVisible = true,
            isLocalButtonChosen = bool
        )
    }


    // CRUD FUNKTIONEN
    fun saveMediaItem() {
        viewModelScope.launch {
            if (bottomSheetUiState.isLocalButtonChosen) {
                if (validateInput()) {
                    val mediaItem = checkTitle(bottomSheetUiState.mediaItemDetails).toMediaItem()
                    mediaItemRepository.insertMediaItem(mediaItem)
                } else {
                    alertDialogUiState = AlertDialogUiState( isDialogVisible = true )
                }
            } else {
                if (validateInput()) {
                    val mediaItem = checkTitle(bottomSheetUiState.mediaItemDetails).toMediaItem()
                    mediaItemRepository.sendRemoteItem(
                        mediaItem.copy(
                            remoteId = UUID.randomUUID()
                        ).toDto()
                    )
                    mediaItemRepository.refreshItems()
                } else {
                    alertDialogUiState = AlertDialogUiState( isDialogVisible = true )
                }
            }

        }
    }

    suspend fun deleteRemoteItem(mediaItem: MediaItem) {
        mediaItemRepository.removeRemoteItem(mediaItem.toDto())
        mediaItemRepository.refreshItems()
    }

    suspend fun deleteItem(mediaItem: MediaItem) {
        if(mediaItem.remoteId.toString() == "00000000-0000-0000-0000-000000000000") {
            mediaItemRepository.deleteItem(mediaItem)
        } else {
            deleteRemoteItem(mediaItem)
        }
    }

    suspend fun updateItem(mediaItem: MediaItem) {
        if (mediaItem.remoteId.toString() == "00000000-0000-0000-0000-000000000000"){
            mediaItemRepository.updateMediaItem(mediaItem)
        } else {
            updateRemoteItem(mediaItem)
        }

    }

    suspend fun updateRemoteItem(mediaItem: MediaItem) {
        mediaItemRepository.updateRemoteItem(mediaItem)
        mediaItemRepository.refreshItems()
    }

    // HELPER
    private fun validateInput(uiState: MediaItemDetails = bottomSheetUiState.mediaItemDetails): Boolean {
        return with(uiState) {
            src?.isNotEmpty() ?: false
        }
    }

    private fun checkTitle(mediaItem: MediaItemDetails = bottomSheetUiState.mediaItemDetails): MediaItemDetails {
        val item: MediaItemDetails = if (mediaItem.title.isBlank()) {
            MediaItemDetails(
                title = mediaItem.createDate.toString(),
                remoteId = mediaItem.remoteId,
                src = mediaItem.src,
                createDate = mediaItem.createDate
            )
        } else {
            MediaItemDetails(
                title = mediaItem.title,
                remoteId = mediaItem.remoteId,
                src = mediaItem.src,
                createDate = mediaItem.createDate
            )
        }
        return item
    }

    fun uriToByteArray(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        quality: Int = 80,
    ): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val scale = if (bitmap.width > maxWidth) {
                val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()
                val newHeight = (maxWidth * ratio).toInt()
                bitmap.scale(maxWidth, newHeight)
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scale.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()

            if(scale != bitmap) scale.recycle()

            byteArray
        } catch (ex: Exception) {
            Log.e("ERROR", "FEHLER: ${ex.message}")
            null
        }
    }
}

data class ListState(
    val localButton: Boolean = false,
    val remoteButton: Boolean = false,
    val allButton: Boolean = false
)

data class LocalMediaItemListUiState(
    val mediaItems: List<MediaItem> = emptyList(),
)

data class RemoteItemListUiState(
    val mediaItems: List<MediaItem> = emptyList()
)

data class AllMediaItemsUiState(
    val mediaItems: List<MediaItem> = emptyList()
)

data class BottomSheetUiState(
    val mediaItemDetails: MediaItemDetails = MediaItemDetails(),
    val isBottomSheetVisible: Boolean = false,
    val isEntryValid: Boolean = false,
    val isLocalButtonChosen: Boolean = true,
)

data class AlertDialogUiState(
    val isDialogVisible: Boolean = false,
)

data class MinimalDialogUiState(
    val isMinimalDialogVisible: Boolean = false,
    val mediaItem: MediaItemDetails = MediaItemDetails()
)

data class DeleteDialogUiState(
    val isDeleteDialogVisible: Boolean = false,
    val mediaItem: MediaItemDetails = MediaItemDetails()
)
