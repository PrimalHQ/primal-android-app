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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
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

        targetScope.launch(dispatchers.io()) {
            uniqueUrls.asFlow()
                .map { url ->
                    async {
                        runCatching {
                            withTimeoutOrNull(PRE_CACHE_TIMEOUT) {
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
                .buffer(capacity = MAX_CONCURRENT_PRE_CACHE_FETCHES)
                .collect { deferred -> deferred.await() }
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
        const val MAX_CONCURRENT_PRE_CACHE_FETCHES = 5
    }
}
