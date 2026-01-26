package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.MediaItemRepository
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.MediaItemDTO
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.toMediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.map.GpsCoordinates
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.map.MarkerData
import org.maplibre.android.geometry.LatLng
import java.io.ByteArrayInputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.ifEmpty

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val mediaItemRepository: MediaItemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaDetailUiState())
    var uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    var deleteDetailDialogUiState by mutableStateOf(DeleteDetailDialogUiState())
        private set

    suspend fun getItemById(id: String) {
        mediaItemRepository.getMediaItemStream(id.toLong()).collect { mediaItem ->
            _uiState.value = _uiState.value.copy(
                mediaItem = mediaItem?.toMediaItemDetails() ?: MediaItemDetails()
            )
        }
    }

    suspend fun getRemoteItem(id: String) {
        val remoteMediaItem = mediaItemRepository.getRemoteItem(id)
        _uiState.value = _uiState.value.copy(
            mediaItem = remoteMediaItem.toMediaItemDetails()
        )
    }

    suspend fun deleteItem(item: MediaItemDetails) {
        mediaItemRepository.deleteItem(item.toMediaItem())
    }

    suspend fun deleteRemoteItem(item: MediaItemDTO) {
        mediaItemRepository.removeRemoteItem(item)
    }

    fun changeDeleteDialogUiState(
        dialogUiState: Boolean,
        mediaItem: MediaItemDetails,
    ) {
        deleteDetailDialogUiState =
            DeleteDetailDialogUiState(
                mediaItem = mediaItem,
                isVisible = !dialogUiState
            )
    }


    fun extractGpsFromImage(imageBytes: ByteArray): GpsCoordinates? {
        Log.i("DEBUG GPS", "Extracting GPS from ${imageBytes.size} bytes")
        return try {
            val inputStream = ByteArrayInputStream(imageBytes)
            val exif = ExifInterface(inputStream)

            val latLong = exif.latLong
            Log.i("DEBUG GPS", "latLong result: ${latLong?.contentToString()}")
            //val hasCoordinates = exif.getLatLong(latLong)

            if (latLong != null && latLong.size == 2) {
                GpsCoordinates(
                    latitude = latLong[0],
                    longitude = latLong[1]
                )
            } else {
                Log.w("DEBUG GPS", "No GPS data in EXIF")
                null
            }
        } catch (e: Exception) {
            Log.e("DEBUG GPS", "Error extracting GPS: ${e.message}", e)
            null
        }
    }
}

data class MediaDetailUiState(
    var mediaItem: MediaItemDetails = MediaItemDetails()
)

data class DeleteDetailDialogUiState(
    val mediaItem: MediaItemDetails = MediaItemDetails(),
    var isVisible: Boolean = false
)
