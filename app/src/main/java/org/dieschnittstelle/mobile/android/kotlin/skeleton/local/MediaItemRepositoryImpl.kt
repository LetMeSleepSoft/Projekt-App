package org.dieschnittstelle.mobile.android.kotlin.skeleton.local

import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toDto
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.MediaItemDTO
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.encodeBase64
import java.util.UUID

class MediaItemRepositoryImpl(
    val localMediaItemDAO: MediaItemDao,
    val supabase: SupabaseClient,
) : MediaItemRepository {

    override fun getAllMediaItemsStream(): Flow<List<MediaItem>> =
        localMediaItemDAO.getAllMediaItemsStream()

    override suspend fun getMediaItemStream(id: Long): Flow<MediaItem?> =
        localMediaItemDAO.getMediaItem(id)

    override suspend fun insertMediaItem(mediaItem: MediaItem) =
        localMediaItemDAO.insertMediaItem(mediaItem)

    override suspend fun updateMediaItem(mediaItem: MediaItem) =
        localMediaItemDAO.updateMediaItem(mediaItem)

    override suspend fun deleteItem(mediaItem: MediaItem) =
        localMediaItemDAO.deleteMediaItem(mediaItem)

    private val refreshTrigger = MutableSharedFlow<Unit>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getRemoteItems(): Flow<List<MediaItemDTO>> =
        refreshTrigger.onStart { emit(Unit) }.flatMapLatest {
            flow {
                val result = supabase.from("MediaItems")
                    .select()
                    .decodeList<MediaItemDTO>()
                emit(result)
            }.flowOn(Dispatchers.IO)
        }

    override suspend fun refreshItems() {
        refreshTrigger.emit(Unit)
    }

    override suspend fun getRemoteItem(id: String): MediaItemDTO {
        return supabase.from("MediaItems")
            .select {
                filter {
                    MediaItemDTO::remoteId eq UUID.fromString(id)
                }
            }
            .decodeSingle<MediaItemDTO>()
    }

    override suspend fun sendRemoteItem(mediaItem: MediaItemDTO): PostgrestResult {
        return supabase.from("MediaItems")
            .insert(mediaItem)
    }

    override suspend fun removeRemoteItem(mediaItem: MediaItemDTO): PostgrestResult {
        return supabase.from("MediaItems")
            .delete {
                filter {
                    MediaItemDTO::remoteId eq mediaItem.remoteId
                }
            }
    }

    override suspend fun updateRemoteItem(mediaItem: MediaItem): PostgrestResult {
        val dto = mediaItem.toDto()
        return supabase.from("MediaItems")
            .update(dto) {
                filter {
                    MediaItemDTO::remoteId eq mediaItem.remoteId
                }
            }
    }


}