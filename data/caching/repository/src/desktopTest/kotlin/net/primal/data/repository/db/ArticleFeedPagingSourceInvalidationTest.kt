package net.primal.data.repository.db

import androidx.paging.PagingSource
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import net.primal.data.local.dao.bookmarks.PublicBookmark
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.mutes.ListType
import net.primal.data.local.dao.mutes.MutedItemData
import net.primal.data.local.dao.mutes.MutedItemType
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.reads.ArticleData
import net.primal.data.local.dao.reads.ArticleFeedCrossRef
import net.primal.data.local.dao.reads.ArticleFeedItem
import net.primal.data.local.dao.reads.HighlightData
import net.primal.data.local.db.CachingDatabase
import net.primal.domain.bookmarks.BookmarkType
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Pins the article feed [PagingSource]'s *effective* observed-table set — the tables whose writes
 * invalidate the reads feed and force a full re-run of the feed query.
 *
 * Room 3 codegen observes the return POJO's `@Relation` tables ON TOP of the query's tables, which
 * made the reads feed re-query on app-wide writes (profiles, counts, zaps, highlights, posts, …)
 * from every screen — most notably while reading an article, whose details fetch persists nearly
 * all of those tables. The article feed's [PagingSource] must instead invalidate ONLY on:
 *  - [ArticleFeedCrossRef] — actual feed membership/order changes (page persists, refresh, deletes),
 *  - [MutedItemData] — mute filtering changes,
 *  - [EventUserStats] — the user's own interaction flags rendered live on feed items,
 *  - [PublicBookmark] — the card's bookmark indicator (the bookmark toggle has no optimistic UI state),
 * and must NOT invalidate on writes to any other table the query or its relations read.
 */
class ArticleFeedPagingSourceInvalidationTest {

    @Test
    fun articleFeedPagingSource_invalidates_on_ArticleFeedCrossRef_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.articleFeedsConnections().connect(
                data = listOf(
                    ArticleFeedCrossRef(
                        ownerId = USER_ID,
                        spec = FEED_SPEC,
                        articleATag = "article-2-atag",
                        articleAuthorId = AUTHOR_ID,
                    ),
                ),
            )
            pagingSource.awaitInvalidation(reason = "ArticleFeedCrossRef write (feed membership change)")
        }

    @Test
    fun articleFeedPagingSource_invalidates_on_MutedItemData_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.mutedItems().upsertAll(
                data = setOf(
                    MutedItemData(
                        item = AUTHOR_ID,
                        ownerId = USER_ID,
                        type = MutedItemType.User,
                        listType = ListType.MuteList,
                    ),
                ),
            )
            pagingSource.awaitInvalidation(reason = "MutedItemData write (mute filtering change)")
        }

    @Test
    fun articleFeedPagingSource_invalidates_on_EventUserStats_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.eventUserStats().upsert(
                data = EventUserStats(eventId = ARTICLE_EVENT_ID, userId = USER_ID, liked = true),
            )
            pagingSource.awaitInvalidation(reason = "EventUserStats write (own interaction flags)")
        }

    @Test
    fun articleFeedPagingSource_invalidates_on_PublicBookmark_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.publicBookmarks().upsertBookmarks(
                data = listOf(
                    PublicBookmark(
                        tagValue = ARTICLE_A_TAG,
                        tagType = "a",
                        bookmarkType = BookmarkType.Article,
                        ownerId = USER_ID,
                    ),
                ),
            )
            pagingSource.awaitInvalidation(
                reason = "PublicBookmark write (bookmark toggle must update the card's bookmark state)",
            )
        }

    @Test
    fun articleFeedPagingSource_doesNotInvalidate_on_ArticleData_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.articles().upsertAll(
                list = listOf(articleData(aTag = "unrelated-atag", articleId = "unrelated", authorId = "other")),
            )
            pagingSource.assertNoInvalidation(reason = "ArticleData write (e.g. opening an article)")
        }

    @Test
    fun articleFeedPagingSource_doesNotInvalidate_on_ProfileData_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.profiles().insertOrUpdateAll(
                data = listOf(profileData(ownerId = AUTHOR_ID).copy(displayName = "updated")),
            )
            pagingSource.assertNoInvalidation(reason = "ProfileData write (profile refresh)")
        }

    @Test
    fun articleFeedPagingSource_doesNotInvalidate_on_EventStats_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.eventStats().upsert(data = EventStats(eventId = ARTICLE_EVENT_ID, likes = 42))
            pagingSource.assertNoInvalidation(reason = "EventStats write (count update)")
        }

    @Test
    fun articleFeedPagingSource_doesNotInvalidate_on_EventZap_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.eventZaps().insert(
                data = EventZap(
                    eventId = ARTICLE_EVENT_ID,
                    zapSenderId = USER_ID,
                    zapReceiverId = AUTHOR_ID,
                    zapRequestAt = 1_700_000_000L,
                    zapReceiptAt = 1_700_000_000L,
                    amountInBtc = 0.0001,
                    message = null,
                    invoice = null,
                ),
            )
            pagingSource.assertNoInvalidation(reason = "EventZap write (zap updates)")
        }

    @Test
    fun articleFeedPagingSource_doesNotInvalidate_on_HighlightData_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.highlights().upsert(
                data = HighlightData(
                    highlightId = "highlight-1",
                    authorId = USER_ID,
                    content = "highlighted text",
                    context = null,
                    alt = null,
                    referencedEventATag = ARTICLE_A_TAG,
                    referencedEventAuthorId = AUTHOR_ID,
                    createdAt = 1_700_000_000L,
                ),
            )
            pagingSource.assertNoInvalidation(reason = "HighlightData write (article highlights fetch)")
        }

    @Test
    fun articleFeedPagingSource_doesNotInvalidate_on_PostData_write() =
        withArticleFeedPagingSource { database, pagingSource ->
            database.posts().upsertAll(data = listOf(postData(postId = "unrelated-note", authorId = "other")))
            pagingSource.assertNoInvalidation(reason = "PostData write (e.g. opening a thread)")
        }

    // ---------------------------------------------------------------------------------------------
    // harness
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a fresh db seeded with a single-article feed, builds the production article feed
     * [PagingSource] (real SQL through [CachingDatabase.articles]), performs the initial load
     * (which arms Room's invalidation observer), then runs [block].
     */
    private fun withArticleFeedPagingSource(
        block: suspend (CachingDatabase, PagingSource<Int, ArticleFeedItem>) -> Unit,
    ) = runBlocking {
        val databaseName = "primal_article_feed_invalidation_${counter++}.db"
        LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
        val database = LocalDatabaseFactory.createDatabase<CachingDatabase>(databaseName = databaseName)
        try {
            database.profiles().insertOrUpdateAll(data = listOf(profileData(ownerId = AUTHOR_ID)))
            database.articles().upsertAll(
                list = listOf(
                    articleData(aTag = ARTICLE_A_TAG, articleId = ARTICLE_ID, authorId = AUTHOR_ID),
                ),
            )
            database.articleFeedsConnections().connect(
                data = listOf(
                    ArticleFeedCrossRef(
                        ownerId = USER_ID,
                        spec = FEED_SPEC,
                        articleATag = ARTICLE_A_TAG,
                        articleAuthorId = AUTHOR_ID,
                    ),
                ),
            )

            val pagingSource = database.articles().feed(spec = FEED_SPEC, userId = USER_ID)

            val initialLoad = pagingSource.load(
                PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
            )
            val page = initialLoad as? PagingSource.LoadResult.Page
                ?: fail("initial load failed: $initialLoad")
            assertTrue(page.data.isNotEmpty(), "expected the seeded article in the initial page")

            block(database, pagingSource)
        } finally {
            database.close()
            LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
        }
    }

    private suspend fun PagingSource<Int, ArticleFeedItem>.awaitInvalidation(reason: String) {
        val invalidated = withTimeoutOrNull(INVALIDATION_TIMEOUT_MS) {
            while (!invalid) delay(POLL_INTERVAL_MS)
            true
        }
        assertTrue(
            actual = invalidated == true,
            message = "expected article feed PagingSource to invalidate on: $reason",
        )
    }

    private suspend fun PagingSource<Int, ArticleFeedItem>.assertNoInvalidation(reason: String) {
        delay(NO_INVALIDATION_GRACE_MS)
        assertFalse(
            actual = invalid,
            message = "article feed PagingSource must NOT invalidate on: $reason",
        )
    }

    private fun articleData(
        aTag: String,
        articleId: String,
        authorId: String,
    ) = ArticleData(
        aTag = aTag,
        eventId = "event-$articleId",
        articleId = articleId,
        authorId = authorId,
        createdAt = 1_700_000_000L,
        content = "article content",
        title = "article title",
        publishedAt = 1_700_000_000L,
        raw = "{}",
    )

    private fun postData(postId: String, authorId: String) =
        PostData(
            postId = postId,
            authorId = authorId,
            createdAt = 1_700_000_000L,
            tags = emptyList(),
            content = "hello nostr",
            uris = emptyList(),
            hashtags = emptyList(),
            sig = "sig",
            raw = "{}",
        )

    private fun profileData(ownerId: String) =
        ProfileData(
            ownerId = ownerId,
            eventId = "metadata-$ownerId",
            createdAt = 1_700_000_000L,
            raw = "{}",
        )

    companion object {
        private const val USER_ID = "user-pubkey"
        private const val AUTHOR_ID = "author-pubkey"
        private const val ARTICLE_ID = "article-1"
        private const val ARTICLE_A_TAG = "30023:author-pubkey:article-1"
        private const val ARTICLE_EVENT_ID = "event-article-1"
        private const val FEED_SPEC = "test-reads-feed-spec"

        private const val INVALIDATION_TIMEOUT_MS = 3_000L
        private const val NO_INVALIDATION_GRACE_MS = 800L
        private const val POLL_INTERVAL_MS = 25L

        private var counter = 0
    }
}
