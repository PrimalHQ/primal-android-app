package net.primal.core.caching

interface MediaCacher {
    fun preCacheUserAvatars(urls: List<String>)
    fun preCacheFeedMedia(urls: List<String>)
}
