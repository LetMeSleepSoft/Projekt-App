package org.dieschnittstelle.mobile.android.kotlin.skeleton.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.item.MediaItemDao
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.item.MediaItemDatabase
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.item.MediaItemRepository
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.item.MediaItemRepositoryImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context):  MediaItemDatabase {
        return Room.databaseBuilder(
            context = context,
            MediaItemDatabase::class.java,
            "mediaItem_Database",
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideMediaItemDao(database: MediaItemDatabase): MediaItemDao {
        return database.mediaItemDAO()
    }

    @Provides
    @Singleton
    fun provideMediaItemRepository(mediaItemDao: MediaItemDao): MediaItemRepository {
        return MediaItemRepositoryImpl(localMediaItemDAO = mediaItemDao)
    }
}