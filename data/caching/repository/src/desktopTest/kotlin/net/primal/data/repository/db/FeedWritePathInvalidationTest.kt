package net.primal.data.repository.db

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeoutOrNull
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.notes.FeedPost
import net.primal.data.local.dao.notes.FeedPostDataCrossRef
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.db.CachingDatabase
import net.primal.data.local.queries.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.repository.UserDataCleanupRepositoryImpl
import net.primal.data.repository.feed.FeedRepositoryImpl
import net.primal.data.repository.feed.paging.FeedSpecInvalidationTracker
import net.primal.data.repository.feed.paging.NoteFeedRemoteMediator
import net.primal.data.repository.feed.processors.FeedProcessor
import net.primal.domain.nostr.NostrEvent
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Pins that every `FeedPostDataCrossRef` write path routes invalidation through
 * [FeedSpecInvalidationTracker] to ITS OWN `(ownerId, feedSpec)` — and, crucially, that a write
 * for one spec no longer invalidates another spec's live [PagingSource] via Room's table-level
 * invalidation (the cause of the feed→profile→back stutter: opening a profile cleared+rewrote
 * the profile feed's crossrefs and regenerated the home feed identically).
 *
 * Write paths covered (the complete set — crossrefs are written nowhere else):
 *  - [FeedProcessor.processAndPersistToDatabase] (mediator REFRESH/APPEND page persists, `replaceFeed`)
 *  - [NoteFeedRemoteMediator.initialize] (`clearFeedSpec` for always-reset specs)
 *  - [FeedRepositoryImpl.removeFeedSpec]
 *  - [FeedRepositoryImpl.deletePostById] (spec-blind → invalidates all)
 *  - [UserDataCleanupRepositoryImpl.clearUserData] (spec-blind → invalidates all)
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
class FeedWritePathInvalidationTest {

    @Test
    fun feedProcessorPersist_forOtherSpec_doesNotInvalidateThisFeedSource() =
        withDatabase { database, tracker ->
            val mainSource = armedTrackedSource(database, tracker, feedSpec = MAIN_SPEC)

            FeedProcessor(feedSpec = PROFILE_SPEC, database = database, invalidationTracker = tracker)
                .processAndPersistToDatabase(
                    userId = USER_ID,
                    response = feedResponse(noteId = "profile-note"),
                    clearFeed = true,
                )

            mainSource.assertNoInvalidation(
                reason = "another spec's feed persist (e.g. opening a profile) " +
                    "must NOT regenerate this feed",
            )
        }

    @Test
    fun feedProcessorPersist_forSameSpec_invalidatesItsFeedSource() =
        withDatabase { database, tracker ->
            val mainSource = armedTrackedSource(database, tracker, feedSpec = MAIN_SPEC)

            FeedProcessor(feedSpec = MAIN_SPEC, database = database, invalidationTracker = tracker)
                .processAndPersistToDatabase(
                    userId = USER_ID,
                    response = feedResponse(noteId = "new-note"),
                    clearFeed = false,
                )

            mainSource.awaitInvalidation(reason = "own feed page persist")
        }

    @Test
    fun mediatorInitialize_alwaysResetSpec_invalidatesOnlyItsOwnFeed() =
        withDatabase { database, tracker ->
            val mainSource = armedTrackedSource(database, tracker, feedSpec = MAIN_SPEC)
            val profileSource = armedTrackedSource(database, tracker, feedSpec = PROFILE_SPEC)

            noteFeedRemoteMediator(database, tracker, feedSpec = PROFILE_SPEC).initialize()

            profileSource.awaitInvalidation(reason = "own clearFeedSpec at mediator initialize")
            mainSource.assertNoInvalidation(
                reason = "another feed's clearFeedSpec at mediator initialize (profile open)",
            )
        }

    @Test
    fun removeFeedSpec_invalidatesItsFeedSource() =
        withDatabase { database, tracker ->
            val mainSource = armedTrackedSource(database, tracker, feedSpec = MAIN_SPEC)

            feedRepository(database, tracker).removeFeedSpec(userId = USER_ID, feedSpec = MAIN_SPEC)

            mainSource.awaitInvalidation(reason = "removeFeedSpec for this spec")
        }

    @Test
    fun deletePostById_invalidatesAllFeedSources() =
        withDatabase { database, tracker ->
            val mainSource = armedTrackedSource(database, tracker, feedSpec = MAIN_SPEC)
            val profileSource = armedTrackedSource(database, tracker, feedSpec = PROFILE_SPEC)

            feedRepository(database, tracker).deletePostById(postId = NOTE_ID, userId = USER_ID)

            mainSource.awaitInvalidation(reason = "note deletion (spec-blind crossref delete)")
            profileSource.awaitInvalidation(reason = "note deletion (spec-blind crossref delete)")
        }

    @Test
    fun clearUserData_invalidatesAllFeedSources() =
        withDatabase { database, tracker ->
            val mainSource = armedTrackedSource(database, tracker, feedSpec = MAIN_SPEC)

            UserDataCleanupRepositoryImpl(database = database, invalidationTracker = tracker)
                .clearUserData(userId = USER_ID)

            mainSource.awaitInvalidation(reason = "account data cleanup (spec-blind crossref delete)")
        }

    // ---------------------------------------------------------------------------------------------
    // harness
    // ---------------------------------------------------------------------------------------------

    private fun withDatabase(block: suspend (CachingDatabase, FeedSpecInvalidationTracker) -> Unit) =
        runBlocking {
            val databaseName = "primal_feed_write_path_${counter++}.db"
            LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
            val database = LocalDatabaseFactory.createDatabase<CachingDatabase>(databaseName = databaseName)
            try {
                database.profiles().insertOrUpdateAll(data = listOf(profileData(ownerId = AUTHOR_ID)))
                database.posts().upsertAll(data = listOf(postData(postId = NOTE_ID, authorId = AUTHOR_ID)))
                database.feedsConnections().connect(
                    data = listOf(
                        FeedPostDataCrossRef(ownerId = USER_ID, feedSpec = MAIN_SPEC, eventId = NOTE_ID),
                        FeedPostDataCrossRef(ownerId = USER_ID, feedSpec = PROFILE_SPEC, eventId = NOTE_ID),
                    ),
                )
                block(database, FeedSpecInvalidationTracker())
            } finally {
                database.close()
                LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
            }
        }

    /**
     * Builds the production feed [PagingSource] for [feedSpec], registers it with the tracker,
     * and performs the initial load — which arms Room's invalidation observer, so the
     * "no invalidation" assertions genuinely prove Room no longer fires for crossref writes.
     */
    private suspend fun armedTrackedSource(
        database: CachingDatabase,
        tracker: FeedSpecInvalidationTracker,
        feedSpec: String,
    ): PagingSource<Int, FeedPost> {
        val pagingSource = tracker.track(
            ownerId = USER_ID,
            feedSpec = feedSpec,
            pagingSource = database.feedPosts().feedQuery(
                query = ChronologicalFeedWithRepostsQueryBuilder(
                    feedSpec = feedSpec,
                    userPubkey = USER_ID,
                    allowMutedThreads = false,
                ).feedQuery(),
            ),
        )
        val initialLoad = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )
        val page = initialLoad as? PagingSource.LoadResult.Page ?: fail("initial load failed: $initialLoad")
        assertTrue(page.data.isNotEmpty(), "expected the seeded post in the initial page")
        return pagingSource
    }

    private fun feedRepository(database: CachingDatabase, tracker: FeedSpecInvalidationTracker) =
        FeedRepositoryImpl(
            feedApi = mockk<FeedApi>(),
            database = database,
            dispatcherProvider = testDispatcherProvider(),
            invalidationTracker = tracker,
        )

    private fun noteFeedRemoteMediator(
        database: CachingDatabase,
        tracker: FeedSpecInvalidationTracker,
        feedSpec: String,
    ) = NoteFeedRemoteMediator(
        dispatcherProvider = testDispatcherProvider(),
        feedSpec = feedSpec,
        userId = USER_ID,
        feedApi = mockk<FeedApi>(),
        database = database,
        invalidationTracker = tracker,
    )

    private fun testDispatcherProvider(): DispatcherProvider {
        val testDispatcher = UnconfinedTestDispatcher()
        return mockk<DispatcherProvider> {
            every { io() } returns testDispatcher
            every { main() } returns testDispatcher
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

    private fun feedResponse(noteId: String) =
        FeedResponse(
            paging = null,
            metadata = emptyList(),
            notes = listOf(
                NostrEvent(
                    id = noteId,
                    pubKey = AUTHOR_ID,
                    createdAt = 1_700_000_000L,
                    kind = 1,
                    tags = emptyList(),
                    content = "hello nostr",
                    sig = "signature",
                ),
            ),
            articles = emptyList(),
            reposts = emptyList(),
            zaps = emptyList(),
            referencedEvents = emptyList(),
            primalEventStats = emptyList(),
            primalEventUserStats = emptyList(),
            cdnResources = emptyList(),
            primalLinkPreviews = emptyList(),
            primalRelayHints = emptyList(),
            blossomServers = emptyList(),
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
        private const val NOTE_ID = "note-1"
        private const val MAIN_SPEC = """{"id":"latest","kind":"notes"}"""
        private val PROFILE_SPEC =
            """{"id":"feed","kind":"notes","notes":"authored","pubkey":"${"a".repeat(64)}"}"""

        private const val INVALIDATION_TIMEOUT_MS = 3_000L
        private const val NO_INVALIDATION_GRACE_MS = 800L
        private const val POLL_INTERVAL_MS = 25L

        private var counter = 0
    }
}
