package org.dieschnittstelle.mobile.android.kotlin.skeleton.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem

@Database(entities = [MediaItem::class], version = 7, exportSchema = false)
abstract class MediaItemDatabase : RoomDatabase() {
    abstract fun mediaItemDAO(): MediaItemDao
}