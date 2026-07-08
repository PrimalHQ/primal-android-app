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
import net.primal.data.local.dao.reads.ArticleFeedCrossRef

/**
 * A [PagingSource] return-type converter for the article feed DAO that narrows the observed-table
 * set, the reads-feed counterpart of [FeedPagingSourceDaoReturnTypeConverter].
 *
 * Room 3 codegen observes the return POJO's `@Relation` tables on top of the query's tables — for
 * `ArticleDao.feed` that is 8 app-wide tables (ArticleData, ArticleFeedCrossRef, EventUserStats,
 * MutedItemData, ProfileData, EventStats, EventZap, PublicBookmark), so writes from unrelated
 * screens invalidate every live reads feed. Worst of all, opening an article details screen
 * persists profiles, stats, zaps and articles, guaranteeing the reads feed is invalidated while
 * the user is reading and re-runs the full multi-relation query on back-navigation.
 *
 * The article feed must re-query only when its rendered output can actually change live:
 *  - [ArticleFeedCrossRef] — feed membership/order (page persists, refresh, deletes),
 *  - [MutedItemData] — mute filtering in the feed SQL's WHERE clause,
 *  - [EventUserStats] — the user's own interaction flags (liked/replied/reposted/zapped),
 *  - [PublicBookmark] — the card's bookmark state; the bookmark toggle has no optimistic UI
 *    state, so this write is the only signal that flips the indicator.
 *
 * Everything else the query or its relations read is refreshed on the next structural
 * invalidation instead of live. Behavior is pinned by `ArticleFeedPagingSourceInvalidationTest`
 * in `:data:caching:repository` desktopTest.
 */
class ArticleFeedPagingSourceDaoReturnTypeConverter {

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
            tableNames = ARTICLE_FEED_OBSERVED_TABLES,
            roomRawQuery = roomRawQuery,
            executeAndConvert = executeAndConvert,
        )

    companion object {
        private val ARTICLE_FEED_OBSERVED_TABLES =
            arrayOf(
                ArticleFeedCrossRef::class,
                MutedItemData::class,
                EventUserStats::class,
                PublicBookmark::class,
            )
                .map { requireNotNull(it.simpleName) }
                .toTypedArray()
    }
}
