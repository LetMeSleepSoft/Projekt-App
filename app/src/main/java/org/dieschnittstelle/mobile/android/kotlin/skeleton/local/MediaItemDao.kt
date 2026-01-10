package org.dieschnittstelle.mobile.android.kotlin.skeleton.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem

@Dao
interface MediaItemDao {

    @Insert
    suspend fun insertMediaItem(mediaItem: MediaItem)

    @Update
    suspend fun updateMediaItem(mediaItem: MediaItem)

    @Delete
    suspend fun deleteMediaItem(mediaItem: MediaItem)

    @Query("SELECT * FROM mediaItems WHERE id = :id")
    fun getMediaItem(id: Long): Flow<MediaItem?>

    @Query("SELECT * FROM mediaItems")
    fun getAllMediaItemsStream(): Flow<List<MediaItem>>
}