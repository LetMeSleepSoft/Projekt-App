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
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.item.MediaItemRepository
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItem
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import androidx.core.graphics.scale
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItemDetails

@HiltViewModel
class MediaItemViewModel @Inject constructor(
    private val mediaItemRepository: MediaItemRepository
) : ViewModel() {

    var alertDialogUiState by mutableStateOf(AlertDialogUiState())
        private set

    var minimalDialogUiState by mutableStateOf(MinimalDialogUiState())
        private set

    var deleteDialogUiState by mutableStateOf(DeleteDialogUiState())
        private set

    var bottomSheetUiState by mutableStateOf(BottomSheetUiState())
        private set

    val mediaItemListUiState: StateFlow<MediaItemListUiState> =
        mediaItemRepository.getAllMediaItemsStream().map { MediaItemListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = MediaItemListUiState()
            )


    fun saveMediaItem() {
        viewModelScope.launch {
            if (validateInput()) {
                val mediaItem = checkTitle(bottomSheetUiState.mediaItemDetails).toMediaItem()
                mediaItemRepository.insertMediaItem(mediaItem)
            } else {
                alertDialogUiState = AlertDialogUiState( isDialogVisible = true )
            }
        }
    }

    suspend fun deleteItem(mediaItem: MediaItem) {
        mediaItemRepository.deleteItem(mediaItem)
    }

    suspend fun updateItem(mediaItem: MediaItem) {
        mediaItemRepository.updateMediaItem(mediaItem)
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

            bitmap.recycle()
            if(scale != bitmap) scale.recycle()

            byteArray
        } catch (ex: Exception) {
            Log.e("ERROR", "FEHLER: ${ex.message}")
            null
        }
    }

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

    private fun validateInput(uiState: MediaItemDetails = bottomSheetUiState.mediaItemDetails): Boolean {
        return with(uiState) {
            src?.isNotEmpty() ?: false
        }
    }

    private fun checkTitle(mediaItem: MediaItemDetails = bottomSheetUiState.mediaItemDetails): MediaItemDetails {
        val item: MediaItemDetails = if (mediaItem.title.isBlank()) {
            MediaItemDetails(
                title = mediaItem.createDate.toString(),
                src = mediaItem.src,
                createDate = mediaItem.createDate
            )
        } else {
            MediaItemDetails(
                title = mediaItem.title,
                src = mediaItem.src,
                createDate = mediaItem.createDate
            )
        }
       return item
    }

    fun updateBottomSheetUiState(mediaItem: MediaItemDetails) {
        bottomSheetUiState = BottomSheetUiState(
            mediaItemDetails = mediaItem,
            isBottomSheetVisible = true,
            isEntryValid = validateInput(mediaItem)
        )
    }
}

data class MediaItemListUiState(
    val mediaItems: List<MediaItem> = emptyList()
)

data class BottomSheetUiState(
    val mediaItemDetails: MediaItemDetails = MediaItemDetails(),
    val isBottomSheetVisible: Boolean = false,
    val isEntryValid: Boolean = false,
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
