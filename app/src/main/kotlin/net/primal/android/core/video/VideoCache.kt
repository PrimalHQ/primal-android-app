package net.primal.android.core.video

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object VideoCache {
    @Volatile
    private var instance: Cache? = null
    private const val CACHE_SIZE_BYTES = 500L * 1024 * 1024

    fun getInstance(context: Context): Cache {
        return instance ?: synchronized(this) {
            instance ?: createCache(context).also { instance = it }
        }
    }

    fun release() {
        instance?.release()
        instance = null
    }

    private fun createCache(context: Context): Cache {
        val databaseProvider = StandaloneDatabaseProvider(context)

        val cacheDirectory = File(context.cacheDir, "primal_video_cache")
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES)

        return SimpleCache(
            cacheDirectory,
            cacheEvictor,
            databaseProvider,
        )
    }
}
