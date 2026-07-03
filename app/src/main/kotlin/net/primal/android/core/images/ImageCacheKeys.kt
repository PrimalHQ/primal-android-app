package net.primal.android.core.images

import coil3.memory.MemoryCache
import coil3.request.ImageRequest

fun ImageRequest.Builder.seedMemoryCache(url: String?): ImageRequest.Builder =
    apply {
        if (url != null) {
            val key = MemoryCache.Key(url)
            memoryCacheKey(key)
            placeholderMemoryCacheKey(key)
        }
    }
