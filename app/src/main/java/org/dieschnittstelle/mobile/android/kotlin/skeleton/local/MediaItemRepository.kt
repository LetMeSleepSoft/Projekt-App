package org.dieschnittstelle.mobile.android.kotlin.skeleton.local

import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.flow.Flow
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.MediaItemDTO

interface MediaItemRepository {

    fun getAllMediaItemsStream(): Flow<List<MediaItem>>

    suspend fun getMediaItemStream(id: Long): Flow<MediaItem?>

    suspend fun insertMediaItem(mediaItem: MediaItem)

    suspend fun updateMediaItem(mediaItem: MediaItem)

    suspend fun deleteItem(mediaItem: MediaItem)

    fun getRemoteItems(): Flow<List<MediaItemDTO>>

    suspend fun refreshItems()

    suspend fun getRemoteItem(id: String): MediaItemDTO

    suspend fun sendRemoteItem(mediaItem: MediaItemDTO): PostgrestResult

    suspend fun removeRemoteItem(mediaItem: MediaItemDTO): PostgrestResult

    suspend fun updateRemoteItem(mediaItem: MediaItem): PostgrestResult
}