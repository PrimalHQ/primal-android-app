package net.primal.core.utils.cache

import androidx.collection.LruCache

class LruSeenCache<K : Any>(maxEntries: Int) : LruCache<K, Unit>(maxSize = maxEntries) {
    fun mark(k: K) = put(k, Unit)
    fun seen(k: K) = get(k) != null
}
