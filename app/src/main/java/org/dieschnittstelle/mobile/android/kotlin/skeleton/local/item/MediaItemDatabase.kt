package org.dieschnittstelle.mobile.android.kotlin.skeleton.local.item

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem

@Database(entities = [MediaItem::class], version = 2, exportSchema = false)
abstract class MediaItemDatabase : RoomDatabase() {
    abstract fun mediaItemDAO(): MediaItemDao
}