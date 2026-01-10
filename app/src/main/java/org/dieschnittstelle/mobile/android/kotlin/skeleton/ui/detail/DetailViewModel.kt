package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.MediaItemRepository
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.MediaItemDTO
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.toMediaItemDetails
import java.util.UUID
import javax.inject.Inject

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
}

data class MediaDetailUiState(
    var mediaItem: MediaItemDetails = MediaItemDetails()
)

data class DeleteDetailDialogUiState(
    val mediaItem: MediaItemDetails = MediaItemDetails(),
    var isVisible: Boolean = false
)
