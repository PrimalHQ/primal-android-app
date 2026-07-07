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
import net.primal.data.local.dao.mutes.ListType
import net.primal.data.local.dao.mutes.MutedItemData
import net.primal.data.local.dao.mutes.MutedItemType
import net.primal.data.local.dao.notes.FeedPost
import net.primal.data.local.dao.notes.FeedPostDataCrossRef
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.db.CachingDatabase
import net.primal.data.local.queries.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.domain.bookmarks.BookmarkType
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Pins the feed [PagingSource]'s *effective* observed-table set — the tables whose writes invalidate the
 * feed and force a full re-run of the 12-relation feed query.
 *
 * Room 3 codegen observes the return POJO's `@Relation` tables ON TOP of the declared `observedEntities`,
 * which made the feed re-query on app-wide writes (profiles, counts, zaps, streams, …) from every screen.
 * The feed's `PagingSource` must instead invalidate ONLY on:
 *  - [FeedPostDataCrossRef] — actual feed membership/order changes (page persists, refresh, deletes),
 *  - [MutedItemData] — mute filtering changes,
 *  - [EventUserStats] — the user's own interaction flags rendered live on feed items,
 *  - [PublicBookmark] — the card's bookmark indicator (the bookmark toggle has no optimistic UI state),
 * and must NOT invalidate on writes to any other table the query or its relations read.
 */
class FeedPagingSourceInvalidationTest {

    @Test
    fun feedPagingSource_invalidates_on_FeedPostDataCrossRef_write() =
        withFeedPagingSource { database, pagingSource ->
            database.feedsConnections().connect(
                data = listOf(
                    FeedPostDataCrossRef(ownerId = USER_ID, feedSpec = FEED_SPEC, eventId = "note-2"),
                ),
            )
            pagingSource.awaitInvalidation(reason = "FeedPostDataCrossRef write (feed membership change)")
        }

    @Test
    fun feedPagingSource_invalidates_on_MutedItemData_write() =
        withFeedPagingSource { database, pagingSource ->
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
    fun feedPagingSource_invalidates_on_EventUserStats_write() =
        withFeedPagingSource { database, pagingSource ->
            database.eventUserStats().upsert(
                data = EventUserStats(eventId = NOTE_ID, userId = USER_ID, liked = true),
            )
            pagingSource.awaitInvalidation(reason = "EventUserStats write (own interaction flags)")
        }

    @Test
    fun feedPagingSource_invalidates_on_PublicBookmark_write() =
        withFeedPagingSource { database, pagingSource ->
            database.publicBookmarks().upsertBookmarks(
                data = listOf(
                    PublicBookmark(
                        tagValue = NOTE_ID,
                        tagType = "e",
                        bookmarkType = BookmarkType.Note,
                        ownerId = USER_ID,
                    ),
                ),
            )
            pagingSource.awaitInvalidation(
                reason = "PublicBookmark write (bookmark toggle must update the card's bookmark state)",
            )
        }

    @Test
    fun feedPagingSource_doesNotInvalidate_on_PostData_write() =
        withFeedPagingSource { database, pagingSource ->
            database.posts().upsertAll(data = listOf(postData(postId = "unrelated-note", authorId = "other")))
            pagingSource.assertNoInvalidation(reason = "PostData write (e.g. opening a thread)")
        }

    @Test
    fun feedPagingSource_doesNotInvalidate_on_ProfileData_write() =
        withFeedPagingSource { database, pagingSource ->
            database.profiles().insertOrUpdateAll(
                data = listOf(profileData(ownerId = AUTHOR_ID).copy(displayName = "updated")),
            )
            pagingSource.assertNoInvalidation(reason = "ProfileData write (profile refresh)")
        }

    @Test
    fun feedPagingSource_doesNotInvalidate_on_EventStats_write() =
        withFeedPagingSource { database, pagingSource ->
            database.eventStats().upsert(data = EventStats(eventId = NOTE_ID, likes = 42))
            pagingSource.assertNoInvalidation(reason = "EventStats write (count update)")
        }

    // ---------------------------------------------------------------------------------------------
    // harness
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a fresh db seeded with a single-post feed, builds the production feed [PagingSource]
     * (real [ChronologicalFeedWithRepostsQueryBuilder] SQL through [CachingDatabase.feedPosts]),
     * performs the initial load (which arms Room's invalidation observer), then runs [block].
     */
    private fun withFeedPagingSource(block: suspend (CachingDatabase, PagingSource<Int, FeedPost>) -> Unit) =
        runBlocking {
            val databaseName = "primal_feed_invalidation_${counter++}.db"
            LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
            val database = LocalDatabaseFactory.createDatabase<CachingDatabase>(databaseName = databaseName)
            try {
                database.profiles().insertOrUpdateAll(data = listOf(profileData(ownerId = AUTHOR_ID)))
                database.posts().upsertAll(data = listOf(postData(postId = NOTE_ID, authorId = AUTHOR_ID)))
                database.feedsConnections().connect(
                    data = listOf(FeedPostDataCrossRef(ownerId = USER_ID, feedSpec = FEED_SPEC, eventId = NOTE_ID)),
                )

                val query = ChronologicalFeedWithRepostsQueryBuilder(
                    feedSpec = FEED_SPEC,
                    userPubkey = USER_ID,
                    allowMutedThreads = false,
                ).feedQuery()
                val pagingSource = database.feedPosts().feedQuery(query = query)

                val initialLoad = pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
                )
                val page = initialLoad as? PagingSource.LoadResult.Page
                    ?: fail("initial load failed: $initialLoad")
                assertTrue(page.data.isNotEmpty(), "expected the seeded post in the initial page")

                block(database, pagingSource)
            } finally {
                database.close()
                LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
            }
        }

    private suspend fun PagingSource<Int, FeedPost>.awaitInvalidation(reason: String) {
        val invalidated = withTimeoutOrNull(INVALIDATION_TIMEOUT_MS) {
            while (!invalid) delay(POLL_INTERVAL_MS)
            true
        }
        assertTrue(
            actual = invalidated == true,
            message = "expected feed PagingSource to invalidate on: $reason",
        )
    }

    private suspend fun PagingSource<Int, FeedPost>.assertNoInvalidation(reason: String) {
        delay(NO_INVALIDATION_GRACE_MS)
        assertFalse(
            actual = invalid,
            message = "feed PagingSource must NOT invalidate on: $reason",
        )
    }

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
        private const val NOTE_ID = "note-1"
        private const val FEED_SPEC = "test-feed-spec"

        private const val INVALIDATION_TIMEOUT_MS = 3_000L
        private const val NO_INVALIDATION_GRACE_MS = 800L
        private const val POLL_INTERVAL_MS = 25L

        private var counter = 0
    }
}
