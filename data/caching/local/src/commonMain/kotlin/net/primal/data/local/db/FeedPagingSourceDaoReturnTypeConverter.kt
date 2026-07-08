package net.primal.data.local.db

import androidx.paging.PagingSource
import androidx.room3.DaoReturnTypeConverter
import androidx.room3.OperationType
import androidx.room3.RoomDatabase
import androidx.room3.RoomRawQuery
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.mutes.MutedItemData
import net.primal.data.local.dao.notes.FeedPostDataCrossRef

/**
 * A [PagingSource] return-type converter for the feed DAO that narrows the observed-table set.
 *
 * Room 3 codegen observes the return POJO's `@Relation` tables on top of the declared
 * `observedEntities` — for `FeedPostDao.feedQuery` that is 12 app-wide tables (EventUri,
 * EventUriNostr, ProfileData, EventStats, EventRelayHints, EventZap, PublicBookmark, StreamData,
 * PollData, PostData, MutedItemData, EventUserStats), so writes from unrelated screens (profile
 * refreshes, count updates, the stream firehose, opening a thread) invalidate every live feed
 * and re-run the full 12-relation query. `observedEntities` can only add tables, never remove
 * the relation-derived ones, so the narrowing has to happen here, at PagingSource construction.
 *
 * The feed must re-query only when its rendered output can actually change live:
 *  - `FeedPostDataCrossRef` — feed membership/order (page persists, refresh, deletes),
 *  - `MutedItemData` — mute filtering in the feed SQL's WHERE clause,
 *  - `EventUserStats` — the user's own interaction flags (liked/replied/reposted/zapped/voted),
 *  - `PublicBookmark` — the card's bookmark state; the bookmark toggle has no optimistic UI
 *    state, so this write is the only signal that flips the indicator. Low-churn table
 *    (bookmark actions + bookmark-list sync only), so observing it is cheap.
 *
 * Everything else the query or its relations read is refreshed on the next structural
 * invalidation instead of live. Behavior is pinned by `FeedPagingSourceInvalidationTest`
 * in `:data:caching:repository` desktopTest.
 */
class FeedPagingSourceDaoReturnTypeConverter {

    private val delegate = PagingSourceDaoReturnTypeConverter()

    @Suppress("UnusedParameter")
    @DaoReturnTypeConverter(operations = [OperationType.READ])
    fun <T : Any> convert(
        database: RoomDatabase,
        tableNames: Array<String>,
        roomRawQuery: RoomRawQuery,
        executeAndConvert: suspend (RoomRawQuery) -> List<T>,
    ): PagingSource<Int, T> =
        delegate.convert(
            database = database,
            tableNames = FEED_OBSERVED_TABLES,
            roomRawQuery = roomRawQuery,
            executeAndConvert = executeAndConvert,
        )

    companion object {
        private val FEED_OBSERVED_TABLES =
            arrayOf(
                FeedPostDataCrossRef::class,
                MutedItemData::class,
                EventUserStats::class,
                PublicBookmark::class,
            )
                .map { requireNotNull(it.simpleName) }
                .toTypedArray()
    }
}
