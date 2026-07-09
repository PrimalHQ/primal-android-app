package net.primal.data.repository.feed.paging

import androidx.paging.PagingSource
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

/**
 * Routes note-feed [PagingSource] invalidation per `(ownerId, feedSpec)` instead of per table.
 *
 * `FeedPostDataCrossRef` is spec-blind: every feed's rows live in the one table, so Room's
 * table-level invalidation regenerated EVERY live feed whenever ANY feed wrote its own rows
 * (opening a profile cleared+rewrote the profile feed and thereby invalidated the home feed —
 * a full re-query, re-diff and recomposition of an identical list). The table is therefore
 * excluded from the feed's Room-observed set (see `FeedPagingSourceDaoReturnTypeConverter`),
 * and every crossref write path calls [invalidate] with its own spec after the write commits.
 *
 * Sources self-unregister via their invalidated callback, so entries never outlive the next
 * invalidation of their spec.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class FeedSpecInvalidationTracker {

    private data class FeedKey(val ownerId: String, val feedSpec: String)

    private val activeSources = AtomicReference<Map<FeedKey, Set<PagingSource<*, *>>>>(emptyMap())

    fun <Key : Any, Value : Any> track(
        ownerId: String,
        feedSpec: String,
        pagingSource: PagingSource<Key, Value>,
    ): PagingSource<Key, Value> {
        val key = FeedKey(ownerId = ownerId, feedSpec = feedSpec)
        activeSources.update { current -> current + (key to (current[key].orEmpty() + pagingSource)) }
        pagingSource.registerInvalidatedCallback {
            activeSources.update { current ->
                val remaining = current[key].orEmpty() - pagingSource
                if (remaining.isEmpty()) current - key else current + (key to remaining)
            }
        }
        return pagingSource
    }

    fun invalidate(ownerId: String, feedSpec: String) {
        activeSources.load()[FeedKey(ownerId = ownerId, feedSpec = feedSpec)]
            ?.forEach { it.invalidate() }
    }

    fun invalidateAll() {
        activeSources.load().values.forEach { sources ->
            sources.forEach { it.invalidate() }
        }
    }
}
