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

@HiltViewModel
class MediaItemViewModel @Inject constructor(
    private val mediaItemRepository: MediaItemRepository
) : ViewModel() {

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
                Log.i("SAVE CLICKED","ITEM: ${bottomSheetUiState.mediaItemDetails.src}")
                val mediaItem = bottomSheetUiState.mediaItemDetails.toMediaItem()
                mediaItemRepository.insertMediaItem(mediaItem)
            }
        }
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
                Bitmap.createScaledBitmap(
                    bitmap, maxWidth, newHeight, true)
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


    fun changeBottomSheetUiState(sheetState: Boolean) {
        bottomSheetUiState =
            BottomSheetUiState(isBottomSheetVisible = !sheetState)
    }

    private fun validateInput(uiState: MediaItemDetails = bottomSheetUiState.mediaItemDetails): Boolean {
        return with(uiState) {
            title.isNotBlank()
        }
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