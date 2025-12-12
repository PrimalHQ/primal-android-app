package net.primal.android.core.images

import android.content.Context
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.primal.core.caching.MediaCacher
import net.primal.core.utils.coroutines.DispatcherProvider

@Singleton
class CoilMediaCacher @Inject constructor(
    @param:ApplicationContext private val context: Context,
    dispatchers: DispatcherProvider,
    private val avatarImageLoader: ImageLoader,
    private val feedImageLoader: ImageLoader,
) : MediaCacher {

    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())

    override fun preCacheUserAvatars(urls: List<String>) {
        if (urls.isEmpty()) return

        scope.launch {
            withTimeout(PRE_CACHE_TIMEOUT) {
                val uniqueUrls = urls
                    .filter { it.isNotBlank() }
                    .distinct()

                for (url in uniqueUrls) {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .build()

                    avatarImageLoader.enqueue(request)
                }
            }
        }
    }

    override fun preCacheFeedMedia(urls: List<String>) {
        if (urls.isEmpty()) return

        scope.launch {
            withTimeout(PRE_CACHE_TIMEOUT) {
                val uniqueUrls = urls
                    .filter { it.isNotBlank() }
                    .distinct()

                for (url in uniqueUrls) {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .build()

                    feedImageLoader.enqueue(request)
                }
            }
        }
    }

    private companion object {
        val PRE_CACHE_TIMEOUT = 20.seconds
    }
}
