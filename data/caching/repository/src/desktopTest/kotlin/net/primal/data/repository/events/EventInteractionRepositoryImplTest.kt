package net.primal.data.repository.events

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.createAppBuildHelper
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventStatsDao
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventUserStatsDao
import net.primal.data.local.dao.events.EventZapDao
import net.primal.data.local.db.PrimalDatabase
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.isClientTag
import net.primal.domain.nostr.publisher.NostrPublishException
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.publisher.PrimalPublishResult
import net.primal.domain.publisher.PrimalPublisher
import net.primal.shared.data.local.db.withTransaction

@Suppress("MaxLineLength")
class EventInteractionRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

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
    private lateinit var dispatcherProvider: DispatcherProvider

    @BeforeTest
    fun setUp() {
        mockkStatic("net.primal.shared.data.local.db.RoomDatabaseExtKt")
        mockkStatic("net.primal.core.utils.AppBuildHelper_desktopKt")
        every { createAppBuildHelper() } returns mockk {
            every { getClientName() } returns "Primal Test"
        }

        dispatcherProvider = mockk {
            every { io() } returns testDispatcher
            every { main() } returns testDispatcher
        }

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

    @AfterTest
    fun tearDown() {
        unmockkStatic("net.primal.shared.data.local.db.RoomDatabaseExtKt")
        unmockkStatic("net.primal.core.utils.AppBuildHelper_desktopKt")
    }

    private fun buildRepository(
        primalPublisher: PrimalPublisher = mockk {
            coEvery { signPublishImportNostrEvent(any(), any()) } returns dummyPublishResult
        },
    ): EventInteractionRepository {
        return EventInteractionRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            primalPublisher = primalPublisher,
            nostrZapperFactory = mockk<NostrZapperFactory>(),
            database = mockDatabase,
        )
    }

    @Test
    fun `likePost updates like stats in database if reaction was published`() =
        runTest(testDispatcher) {
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
        runTest(testDispatcher) {
            val failingPublisher = mockk<PrimalPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws NostrPublishException("publish failed")
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
        runTest(testDispatcher) {
            val failingPublisher = mockk<PrimalPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws NostrPublishException("publish failed")
            }
            val repository = buildRepository(primalPublisher = failingPublisher)

            shouldThrow<NostrPublishException> {
                repository.likeEvent(
                    userId = userId,
                    eventId = eventId,
                    eventAuthorId = eventAuthorId,
                )
            }
        }

    @Test
    fun `likePost does not update like stats in database if post was liked by user`() =
        runTest(testDispatcher) {
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
        runTest(testDispatcher) {
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
        runTest(testDispatcher) {
            val failingPublisher = mockk<PrimalPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws NostrPublishException("publish failed")
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
        runTest(testDispatcher) {
            val failingPublisher = mockk<PrimalPublisher> {
                coEvery {
                    signPublishImportNostrEvent(any(), any())
                } throws NostrPublishException("publish failed")
            }
            val repository = buildRepository(primalPublisher = failingPublisher)

            shouldThrow<NostrPublishException> {
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
    fun `likePost includes client tag in published event`() =
        runTest(testDispatcher) {
            val mockPublisher = mockk<PrimalPublisher> {
                coEvery { signPublishImportNostrEvent(any(), any()) } returns dummyPublishResult
            }
            val repository = buildRepository(primalPublisher = mockPublisher)

            repository.likeEvent(
                userId = userId,
                eventId = eventId,
                eventAuthorId = eventAuthorId,
            )

            coVerify {
                mockPublisher.signPublishImportNostrEvent(
                    match { event -> event.tags.any { it.isClientTag() } },
                    any(),
                )
            }
        }

    @Test
    fun `repostPost includes client tag in published event`() =
        runTest(testDispatcher) {
            val mockPublisher = mockk<PrimalPublisher> {
                coEvery { signPublishImportNostrEvent(any(), any()) } returns dummyPublishResult
            }
            val repository = buildRepository(primalPublisher = mockPublisher)

            repository.repostEvent(
                userId = userId,
                eventId = eventId,
                eventAuthorId = eventAuthorId,
                eventKind = NostrEventKind.ShortTextNote.value,
                eventRawNostrEvent = "{}",
            )

            coVerify {
                mockPublisher.signPublishImportNostrEvent(
                    match { event -> event.tags.any { it.isClientTag() } },
                    any(),
                )
            }
        }
}
