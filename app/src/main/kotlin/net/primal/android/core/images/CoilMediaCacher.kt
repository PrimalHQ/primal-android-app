package net.primal.android.core.images

import android.content.Context
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import net.primal.core.caching.MediaCacher

private const val PRE_CACHE_DEBOUNCE_DELAY = 500L

@Singleton
class CoilMediaCacher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
) : MediaCacher {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var preCacheJob: Job? = null

    override fun preCacheUserAvatars(urls: List<String>) {
        if (urls.isEmpty()) return

        preCacheJob?.cancel()

        preCacheJob = scope.launch {
            delay(PRE_CACHE_DEBOUNCE_DELAY)

            val uniqueUrls = urls
                .filter { it.isNotBlank() }
                .distinct()

            for (url in uniqueUrls) {
                if (!isActive) break

                val request = ImageRequest.Builder(context)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .build()

                imageLoader.enqueue(request)

                yield()
            }
        }
    }
}
