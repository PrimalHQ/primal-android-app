/*
 * LRU cache for Namecoin name lookups.
 *
 * Ported from Amethyst PR #1734 by mstrofnone.
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin.electrumx

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CachedResult(
    val result: NamecoinNostrResult?,
    val timestamp: Long = System.currentTimeMillis(),
)

class NamecoinLookupCache(
    private val maxEntries: Int = 500,
    private val ttlMs: Long = 3_600_000L, // 1 hour
) {
    private val cache = LinkedHashMap<String, CachedResult>(maxEntries, 0.75f, true)
    private val mutex = Mutex()

    private fun cacheKey(identifier: String): String = identifier.trim().lowercase()

    suspend fun get(identifier: String): CachedResult? = mutex.withLock {
        val key = cacheKey(identifier)
        val entry = cache[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > ttlMs) {
            cache.remove(key)
            return null
        }
        return entry
    }

    suspend fun put(identifier: String, result: NamecoinNostrResult?) = mutex.withLock {
        val key = cacheKey(identifier)
        if (cache.size >= maxEntries) {
            val eldest = cache.entries.firstOrNull()
            if (eldest != null) cache.remove(eldest.key)
        }
        cache[key] = CachedResult(result)
    }

    suspend fun invalidate(identifier: String) = mutex.withLock {
        cache.remove(cacheKey(identifier))
    }

    suspend fun clear() = mutex.withLock {
        cache.clear()
    }
}
