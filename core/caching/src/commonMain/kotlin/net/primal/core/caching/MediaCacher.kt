package net.primal.core.caching

import kotlinx.coroutines.CoroutineScope

interface MediaCacher {
    fun preCacheUserAvatars(urls: List<String>)
    fun preCacheFeedMedia(urls: List<String>, scope: CoroutineScope? = null)
}
