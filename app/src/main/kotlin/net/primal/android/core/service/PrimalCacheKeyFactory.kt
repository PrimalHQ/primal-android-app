package net.primal.android.core.service

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheKeyFactory

@UnstableApi
object PrimalCacheKeyFactory : CacheKeyFactory {
    override fun buildCacheKey(dataSpec: androidx.media3.datasource.DataSpec): String {
        return dataSpec.uri.toString().substringBefore("?")
    }
}
