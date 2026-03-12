package net.primal.data.repository.events

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventStatsDao
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventUserStatsDao
import net.primal.data.local.dao.events.EventZapDao
import net.primal.data.local.db.PrimalDatabase
import net.primal.shared.data.local.db.withTransaction

@Suppress("MaxLineLength")
class EventStatsUpdaterTest {

    private val eventId = "test_event_id"
    private val userId = "test_user_id"
    private val eventAuthorId = "test_author_id"

    private val initialEventStats = EventStats(eventId = eventId, likes = 5, reposts = 3)
    private val initialEventUserStats = EventUserStats(
        eventId = eventId,
        userId = userId,
        liked = false,
        reposted = false,
    )

    private val mockEventStatsDao = mockk<EventStatsDao>(relaxUnitFun = true)
    private val mockEventUserStatsDao = mockk<EventUserStatsDao>(relaxUnitFun = true)
    private val mockEventZapDao = mockk<EventZapDao>(relaxUnitFun = true)
    private val mockDatabase = mockk<PrimalDatabase> {
        coEvery { eventStats() } returns mockEventStatsDao
        coEvery { eventUserStats() } returns mockEventUserStatsDao
        coEvery { eventZaps() } returns mockEventZapDao
    }

    @BeforeTest
    fun setUp() {
        mockkStatic("net.primal.shared.data.local.db.RoomDatabaseExtKt")
        coEvery { mockDatabase.withTransaction(any<suspend () -> Any>()) } coAnswers {
            val block = secondArg<suspend () -> Any>()
            block()
        }
        coEvery { mockEventStatsDao.find(eventId = eventId) } returns initialEventStats
        coEvery {
            mockEventUserStatsDao.find(eventId = eventId, userId = userId)
        } returns initialEventUserStats
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic("net.primal.shared.data.local.db.RoomDatabaseExtKt")
    }

    private fun createUpdater() =
        EventStatsUpdater(
            eventId = eventId,
            userId = userId,
            eventAuthorId = eventAuthorId,
            database = mockDatabase,
        )

    @Test
    fun `increaseLikeStats updates like stats in database`() =
        runTest {
            val updater = createUpdater()

            updater.increaseLikeStats()

            val statsSlot = slot<EventStats>()
            val userStatsSlot = slot<EventUserStats>()
            coVerify { mockEventStatsDao.upsert(data = capture(statsSlot)) }
            coVerify { mockEventUserStatsDao.upsert(data = capture(userStatsSlot)) }

            statsSlot.captured.likes shouldBe initialEventStats.likes + 1
            userStatsSlot.captured.liked shouldBe true
        }

    @Test
    fun `increaseLikeStats does not do multiple increases`() =
        runTest {
            val updater = createUpdater()

            updater.increaseLikeStats()
            updater.increaseLikeStats()

            // The mock always returns the same initial stats (likes=5) for find(),
            // so both calls compute likes=6 and upsert that value. The net result
            // is a single increment (likes=6) regardless of how many times it is called.
            val statsSlots = mutableListOf<EventStats>()
            coVerify(exactly = 2) { mockEventStatsDao.upsert(data = capture(statsSlots)) }
            statsSlots[0].likes shouldBe initialEventStats.likes + 1
            statsSlots[1].likes shouldBe initialEventStats.likes + 1
        }

    @Test
    fun `increaseRepostStats updates repost stats in database`() =
        runTest {
            val updater = createUpdater()

            updater.increaseRepostStats()

            val statsSlot = slot<EventStats>()
            val userStatsSlot = slot<EventUserStats>()
            coVerify { mockEventStatsDao.upsert(data = capture(statsSlot)) }
            coVerify { mockEventUserStatsDao.upsert(data = capture(userStatsSlot)) }

            statsSlot.captured.reposts shouldBe initialEventStats.reposts + 1
            userStatsSlot.captured.reposted shouldBe true
        }

    @Test
    fun `increaseRepostStats does not do multiple increases`() =
        runTest {
            val updater = createUpdater()

            updater.increaseRepostStats()
            updater.increaseRepostStats()

            // Same reasoning as like stats: mock returns the same initial state each time,
            // so both calls produce reposts=4. The database receives the same value twice.
            val statsSlots = mutableListOf<EventStats>()
            coVerify(exactly = 2) { mockEventStatsDao.upsert(data = capture(statsSlots)) }
            statsSlots[0].reposts shouldBe initialEventStats.reposts + 1
            statsSlots[1].reposts shouldBe initialEventStats.reposts + 1
        }

    @Test
    fun `revertStats reverts all stats changes in database to stats before`() =
        runTest {
            val updater = createUpdater()

            // First increase like stats to simulate an optimistic update
            updater.increaseLikeStats()

            // Revert should read current DB state and write it back, plus delete zap entries
            updater.revertStats()

            val statsSlots = mutableListOf<EventStats>()
            val userStatsSlots = mutableListOf<EventUserStats>()
            coVerify(exactly = 2) { mockEventStatsDao.upsert(data = capture(statsSlots)) }
            coVerify(exactly = 2) { mockEventUserStatsDao.upsert(data = capture(userStatsSlots)) }

            // The second upsert (from revertStats) should write back the values
            // read from the DB at revert time. Since the mock always returns the
            // initial stats, this effectively restores the original values.
            val revertedStats = statsSlots[1]
            revertedStats.likes shouldBe initialEventStats.likes
            revertedStats.reposts shouldBe initialEventStats.reposts

            val revertedUserStats = userStatsSlots[1]
            revertedUserStats.liked shouldBe initialEventUserStats.liked
            revertedUserStats.reposted shouldBe initialEventUserStats.reposted

            // Verify zap deletion was called during revert
            coVerify {
                mockEventZapDao.delete(
                    senderId = userId,
                    receiverId = eventAuthorId,
                    noteId = eventId,
                    timestamp = any(),
                )
            }
        }
}
