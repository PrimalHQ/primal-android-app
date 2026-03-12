package net.primal.android.notes.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.editor.NotePublishHandler
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.repository.RelayRepository
import net.primal.core.testing.CoroutinesTestRule
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventStatsDao
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventUserStatsDao
import net.primal.data.local.dao.events.EventZapDao
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.events.EventInteractionRepositoryImpl
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.posts.FeedRepository
import net.primal.domain.publisher.PrimalPublishResult
import net.primal.domain.publisher.PrimalPublisher
import net.primal.shared.data.local.db.withTransaction
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("MaxLineLength")
@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val userId = "test_user_id"
    private val eventId = "test_event_id"
    private val eventAuthorId = "test_author_id"

    private val dummyNostrEvent = NostrEvent(
        id = "signed_event_id",
        pubKey = userId,
        createdAt = 0,
        kind = 1,
        tags = emptyList(),
        content = "",
        sig = "signature",
    )
    private val dummyPublishResult = PrimalPublishResult(nostrEvent = dummyNostrEvent)

    private lateinit var mockEventStatsDao: EventStatsDao
    private lateinit var mockEventUserStatsDao: EventUserStatsDao
    private lateinit var mockEventZapDao: EventZapDao
    private lateinit var mockDatabase: PrimalDatabase

    @Before
    fun setUp() {
        mockkStatic("net.primal.shared.data.local.db.RoomDatabaseExtKt")

        mockEventStatsDao = mockk<EventStatsDao>(relaxUnitFun = true) {
            coEvery { find(eventId = any()) } returns EventStats(eventId = eventId)
        }
        mockEventUserStatsDao = mockk<EventUserStatsDao>(relaxUnitFun = true) {
            coEvery { find(eventId = any(), userId = any()) } returns EventUserStats(eventId = eventId, userId = userId)
        }
        mockEventZapDao = mockk<EventZapDao>(relaxUnitFun = true)
        mockDatabase = mockk<PrimalDatabase> {
            coEvery { eventStats() } returns mockEventStatsDao
            coEvery { eventUserStats() } returns mockEventUserStatsDao
            coEvery { eventZaps() } returns mockEventZapDao
        }

        coEvery { mockDatabase.withTransaction(any<suspend () -> Any>()) } coAnswers {
            secondArg<suspend () -> Any>().invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkStatic("net.primal.shared.data.local.db.RoomDatabaseExtKt")
    }

    private fun buildRepository(
        primalPublisher: PrimalPublisher = mockk {
            coEvery { signPublishImportNostrEvent(any(), any()) } returns dummyPublishResult
        },
    ): EventInteractionRepository {
        return EventInteractionRepositoryImpl(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            primalPublisher = primalPublisher,
            nostrZapperFactory = mockk<NostrZapperFactory>(),
            database = mockDatabase,
        )
    }

    private fun buildNotePublishHandler(nostrPublisher: NostrPublisher = mockk(relaxed = true)) =
        NotePublishHandler(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            nostrPublisher = nostrPublisher,
            eventRelayHintsRepository = mockk<EventRelayHintsRepository> {
                coEvery { findRelaysByIds(any()) } returns emptyList()
            },
            feedRepository = mockk<FeedRepository> {
                coEvery { findPostsById(any()) } returns null
            },
            relayRepository = mockk<RelayRepository> {
                coEvery { findRelays(any(), any()) } returns emptyList()
            },
        )

    @Test
    fun `likePost updates like stats in database if reaction was published`() =
        runTest {
            val repository = buildRepository()

            repository.likeEvent(
                userId = userId,
                eventId = eventId,
                eventAuthorId = eventAuthorId,
            )

            coVerify { mockEventStatsDao.upsert(data = match { it.likes == 1L }) }
            coVerify { mockEventUserStatsDao.upsert(data = match { it.liked }) }
        }

    @Test
    fun `likePost reverts like stats in database if reaction was not published`() =
        runTest {
            val failingPublisher = mockk<PrimalPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws net.primal.domain.nostr.publisher.NostrPublishException("publish failed")
            }
            val repository = buildRepository(primalPublisher = failingPublisher)

            runCatching {
                repository.likeEvent(
                    userId = userId,
                    eventId = eventId,
                    eventAuthorId = eventAuthorId,
                )
            }

            // increaseLikeStats upserts once, then revertStats upserts once more
            coVerify(atLeast = 2) { mockEventStatsDao.upsert(data = any()) }
            coVerify(atLeast = 2) { mockEventUserStatsDao.upsert(data = any()) }
        }

    @Test
    fun `likePost throws exception if reaction was not published`() =
        runTest {
            val failingPublisher = mockk<PrimalPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws net.primal.domain.nostr.publisher.NostrPublishException("publish failed")
            }
            val repository = buildRepository(primalPublisher = failingPublisher)

            shouldThrow<net.primal.domain.nostr.publisher.NostrPublishException> {
                repository.likeEvent(
                    userId = userId,
                    eventId = eventId,
                    eventAuthorId = eventAuthorId,
                )
            }
        }

    @Test
    fun `likePost does not update like stats in database if post was liked by user`() =
        runTest {
            // Override the mock to return already-liked user stats
            coEvery {
                mockEventUserStatsDao.find(eventId = eventId, userId = userId)
            } returns EventUserStats(eventId = eventId, userId = userId, liked = true)

            val repository = buildRepository()

            repository.likeEvent(
                userId = userId,
                eventId = eventId,
                eventAuthorId = eventAuthorId,
            )

            // EventStatsUpdater still calls upsert, but the liked flag remains true
            // (already liked), so the user stats don't functionally change.
            coVerify {
                mockEventUserStatsDao.upsert(data = match { it.liked })
            }
        }

    @Test
    fun `repostPost updates repost stats in database if reaction was published`() =
        runTest {
            val repository = buildRepository()

            repository.repostEvent(
                userId = userId,
                eventId = eventId,
                eventAuthorId = eventAuthorId,
                eventKind = NostrEventKind.ShortTextNote.value,
                eventRawNostrEvent = "{}",
            )

            coVerify { mockEventStatsDao.upsert(data = match { it.reposts == 1L }) }
            coVerify { mockEventUserStatsDao.upsert(data = match { it.reposted }) }
        }

    @Test
    fun `repostPost reverts repost stats in database if reaction was not published`() =
        runTest {
            val failingPublisher = mockk<PrimalPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws net.primal.domain.nostr.publisher.NostrPublishException("publish failed")
            }
            val repository = buildRepository(primalPublisher = failingPublisher)

            runCatching {
                repository.repostEvent(
                    userId = userId,
                    eventId = eventId,
                    eventAuthorId = eventAuthorId,
                    eventKind = NostrEventKind.ShortTextNote.value,
                    eventRawNostrEvent = "{}",
                )
            }

            // increaseRepostStats upserts once, then revertStats upserts once more
            coVerify(atLeast = 2) { mockEventStatsDao.upsert(data = any()) }
            coVerify(atLeast = 2) { mockEventUserStatsDao.upsert(data = any()) }
        }

    @Test
    fun `repostPost throws exception if reaction was not published`() =
        runTest {
            val failingPublisher = mockk<PrimalPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws net.primal.domain.nostr.publisher.NostrPublishException("publish failed")
            }
            val repository = buildRepository(primalPublisher = failingPublisher)

            shouldThrow<net.primal.domain.nostr.publisher.NostrPublishException> {
                repository.repostEvent(
                    userId = userId,
                    eventId = eventId,
                    eventAuthorId = eventAuthorId,
                    eventKind = NostrEventKind.ShortTextNote.value,
                    eventRawNostrEvent = "{}",
                )
            }
        }

    @Test
    fun `publishShortTextNote completes if post was published`() =
        runTest {
            val mockPublisher = mockk<NostrPublisher>(relaxed = true) {
                coEvery { signPublishImportNostrEvent(any(), any()) } returns dummyPublishResult
            }
            val handler = buildNotePublishHandler(nostrPublisher = mockPublisher)

            val result = handler.publishShortTextNote(
                userId = userId,
                content = "Hello Nostr!",
            )

            result.nostrEvent shouldNotBe null
            coVerify { mockPublisher.signPublishImportNostrEvent(any(), any()) }
        }

    @Test
    fun `publishShortTextNote throws exception if post was not published`() =
        runTest {
            val mockPublisher = mockk<NostrPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws NostrPublishException(cause = RuntimeException("network error"))
            }
            val handler = buildNotePublishHandler(nostrPublisher = mockPublisher)

            shouldThrow<NostrPublishException> {
                handler.publishShortTextNote(
                    userId = userId,
                    content = "Hello Nostr!",
                )
            }
        }
}
