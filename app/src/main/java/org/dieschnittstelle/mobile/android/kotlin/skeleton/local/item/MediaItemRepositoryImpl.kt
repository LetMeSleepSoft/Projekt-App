package org.dieschnittstelle.mobile.android.kotlin.skeleton.local.item

import kotlinx.coroutines.flow.Flow
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem

class MediaItemRepositoryImpl(
    val localMediaItemDAO: MediaItemDao
) : MediaItemRepository {

    override fun getAllMediaItemsStream(): Flow<List<MediaItem>> =
        localMediaItemDAO.getAllMediaItemsStream()

    override fun getMediaItemStream(id: Long): Flow<MediaItem?> =
        localMediaItemDAO.getMediaItem(id)

    override suspend fun insertMediaItem(mediaItem: MediaItem) =
        localMediaItemDAO.insertMediaItem(mediaItem)

    override suspend fun updateMediaItem(mediaItem: MediaItem) =
        localMediaItemDAO.updateMediaItem(mediaItem)

    override suspend fun deleteItem(mediaItem: MediaItem) =
        localMediaItemDAO.deleteMediaItem(mediaItem)

}