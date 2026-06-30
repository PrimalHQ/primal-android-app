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
import net.primal.core.utils.runCatching

@Singleton
class CoilMediaCacher @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
    private val avatarImageLoader: ImageLoader,
    private val feedImageLoader: ImageLoader,
) : MediaCacher {

    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())

    override fun preCacheUserAvatars(urls: List<String>) {
        val uniqueUrls = urls.filter { it.isNotBlank() }.distinct()
        if (uniqueUrls.isEmpty()) return

        scope.launch {
            withTimeout(PRE_CACHE_TIMEOUT) {
                uniqueUrls.forEach { url ->
                    avatarImageLoader.enqueue(
                        request = buildRequest(
                            url = url,
                            memoryCachePolicy = CachePolicy.DISABLED,
                        )
                    )
                }
            }
        }
    }

    override fun preCacheFeedMedia(urls: List<String>, scope: CoroutineScope?) {
        val targetScope = scope ?: this.scope
        val uniqueUrls = urls.filter { it.isNotBlank() }.distinct()
        if (uniqueUrls.isEmpty()) return

        uniqueUrls.forEach { url ->
            targetScope.launch(dispatchers.io()) {
                runCatching {
                    withTimeout(PRE_CACHE_TIMEOUT) {
                        feedImageLoader.execute(
                            request = buildRequest(
                                url = url,
                                memoryCachePolicy = CachePolicy.ENABLED,
                            )
                        )
                    }
                }
            }
        }
    }

    private fun buildRequest(url: String, memoryCachePolicy: CachePolicy): ImageRequest =
        ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(memoryCachePolicy)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()

    private companion object {
        val PRE_CACHE_TIMEOUT = 20.seconds
    }
}
