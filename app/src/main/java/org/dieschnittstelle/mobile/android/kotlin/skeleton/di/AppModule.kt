package org.dieschnittstelle.mobile.android.kotlin.skeleton.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.MediaItemDao
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.MediaItemDatabase
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.MediaItemRepository
import org.dieschnittstelle.mobile.android.kotlin.skeleton.local.MediaItemRepositoryImpl
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
    fun provideMediaItemRepository(mediaItemDao: MediaItemDao, supabase: SupabaseClient): MediaItemRepository {
        return MediaItemRepositoryImpl(localMediaItemDAO = mediaItemDao, supabase = supabase)
    }

    @Provides
    @Singleton
    fun provideSupabase(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://dznnqmoasspcrwnsnavj.supabase.co",
            supabaseKey = "sb_publishable_k2Hu7ffidvvXmnC29sEEdA_cQYLYNfv"
        ) {
            install(Postgrest)
        }
    }
}
