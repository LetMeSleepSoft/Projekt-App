package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.map

import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.MediaItemRepository
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.maplibre.android.geometry.LatLng
import java.io.ByteArrayInputStream
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mediaItemRepository: MediaItemRepository
) : ViewModel() {

    init {
        Log.i("DEBUG INIT","VIEWMODEL ERSTELLT")
    }

    val mapUiState: StateFlow<MapUiState> =
        mediaItemRepository.getAllMediaItemsStream().map {
            Log.i("DEBUG MAPUISTATE","MESSAGE: ${it.size}")
            MapUiState(it)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = MapUiState()
            )

    val markers: StateFlow<List<MarkerData>> = mapUiState.map { state ->
        withContext(Dispatchers.Default) {
            val realMarkers = state.mediaItems.mapNotNull { item ->
                item.src?.let { imageBytes ->
                    extractGpsFromImage(imageBytes)?.let { gps ->
                        MarkerData(
                            position = LatLng(gps.latitude, gps.longitude),
                            title = item.title,
                            id = item.id
                        )
                    }
                }
            }

            realMarkers.ifEmpty {
                listOf(
                    MarkerData(
                        LatLng(52.520008, 13.404954), "Berlin",
                        id = -1
                    ),
                    MarkerData(
                        LatLng(48.137154, 11.576124), "München",
                        id = -1
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList()
    )

    val isLoading: StateFlow<Boolean> = markers.map { it.isEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = true
        )

    private fun extractGpsFromImage(imageBytes: ByteArray): GpsCoordinates? {
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

data class MapUiState(
    val mediaItems: List<MediaItem> = emptyList()
)

data class GpsCoordinates(
    val latitude: Double,
    val longitude: Double
)

