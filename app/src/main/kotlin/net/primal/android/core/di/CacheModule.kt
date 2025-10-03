package net.primal.android.core.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    private const val MAX_CACHE_SIZE_BYTES = 500L * 1024 * 1024

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    @FeedVideoCache
    fun provideFeedVideoCache(@ApplicationContext context: Context): SimpleCache {
        val cacheDirectory = File(context.cacheDir, "feed_video_cache")
        val databaseProvider: DatabaseProvider = StandaloneDatabaseProvider(context)
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
        return SimpleCache(cacheDirectory, cacheEvictor, databaseProvider)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    @StreamVideoCache
    fun provideStreamVideoCache(@ApplicationContext context: Context): SimpleCache {
        val cacheDirectory = File(context.cacheDir, "stream_video_cache")
        val databaseProvider: DatabaseProvider = StandaloneDatabaseProvider(context)
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
        return SimpleCache(cacheDirectory, cacheEvictor, databaseProvider)
    }
}
