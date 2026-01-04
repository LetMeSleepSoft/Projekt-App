package org.dieschnittstelle.mobile.android.kotlin.skeleton.local.item

import kotlinx.coroutines.flow.Flow
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem

interface MediaItemRepository {

    fun getAllMediaItemsStream(): Flow<List<MediaItem>>

    suspend fun getMediaItemStream(id: Long): Flow<MediaItem?>

    suspend fun insertMediaItem(mediaItem: MediaItem)

    suspend fun updateMediaItem(mediaItem: MediaItem)

    suspend fun deleteItem(mediaItem: MediaItem)

}