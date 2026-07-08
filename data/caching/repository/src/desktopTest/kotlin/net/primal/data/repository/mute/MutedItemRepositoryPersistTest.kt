package net.primal.data.repository.mute

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.mutes.ListType
import net.primal.data.local.dao.mutes.MutedItemData
import net.primal.data.local.dao.mutes.MutedItemType
import net.primal.data.local.db.CachingDatabase
import net.primal.data.remote.api.settings.SettingsApi
import net.primal.data.remote.api.settings.model.GetMuteListResponse
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.publisher.PrimalPublisher
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Pins that a mute list fetch persists ONLY when the fetched list differs from the stored one.
 *
 * [MutedItemData] is one of the few tables the feed PagingSources observe, and
 * `fetchAndPersistMuteList` runs on every ProfileDetails open — an unconditional
 * delete + reinsert there invalidates every feed pager on each profile visit.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MutedItemRepositoryPersistTest {

    @Test
    fun fetchAndPersistMuteList_doesNotWriteMutedItems_whenFetchedListIsUnchanged() =
        withRepository { database, repository, settingsApi ->
            coEvery { settingsApi.getMuteList(USER_ID) } returns muteListResponse(mutedPubkeys = listOf(MUTED_A))
            repository.fetchAndPersistMuteList(userId = USER_ID)

            val invalidations = Channel<Set<String>>(capacity = Channel.UNLIMITED)
            val collector = database.invalidationTracker
                .createFlow("MutedItemData", emitInitialState = true)
                .onEach { invalidations.send(it) }
                .launchIn(this)
            try {
                withTimeout(INVALIDATION_TIMEOUT_MS) { invalidations.receive() }

                repository.fetchAndPersistMuteList(userId = USER_ID)
                delay(NO_INVALIDATION_GRACE_MS)
                val invalidation = invalidations.tryReceive()
                assertTrue(
                    actual = invalidation.isFailure,
                    message = "unchanged mute list must NOT rewrite MutedItemData " +
                        "(it invalidates every feed PagingSource), but got: ${invalidation.getOrNull()}",
                )
            } finally {
                collector.cancel()
            }
        }

    @Test
    fun fetchAndPersistMuteList_persistsChanges_whenFetchedListDiffers() =
        withRepository { database, repository, settingsApi ->
            coEvery { settingsApi.getMuteList(USER_ID) } returns muteListResponse(mutedPubkeys = listOf(MUTED_A))
            repository.fetchAndPersistMuteList(userId = USER_ID)

            coEvery { settingsApi.getMuteList(USER_ID) } returns muteListResponse(mutedPubkeys = listOf(MUTED_B))
            repository.fetchAndPersistMuteList(userId = USER_ID)

            val stored = database.mutedItems().getListByOwnerId(ownerId = USER_ID, listType = ListType.MuteList)
            assertEquals(
                expected = setOf(mutedUser(pubkey = MUTED_B)),
                actual = stored.toSet(),
                message = "changed mute list must replace the stored list",
            )
        }

    @Test
    fun fetchAndPersistMuteList_persistsRemovals_whenFetchedListIsEmpty() =
        withRepository { database, repository, settingsApi ->
            coEvery { settingsApi.getMuteList(USER_ID) } returns muteListResponse(mutedPubkeys = listOf(MUTED_A))
            repository.fetchAndPersistMuteList(userId = USER_ID)

            coEvery { settingsApi.getMuteList(USER_ID) } returns muteListResponse(mutedPubkeys = emptyList())
            repository.fetchAndPersistMuteList(userId = USER_ID)

            val stored = database.mutedItems().getListByOwnerId(ownerId = USER_ID, listType = ListType.MuteList)
            assertTrue(
                actual = stored.isEmpty(),
                message = "emptied mute list must clear the stored list, but got: $stored",
            )
        }

    // ---------------------------------------------------------------------------------------------
    // harness
    // ---------------------------------------------------------------------------------------------

    private fun withRepository(
        block: suspend kotlinx.coroutines.CoroutineScope.(
            CachingDatabase,
            MutedItemRepositoryImpl,
            SettingsApi,
        ) -> Unit,
    ) = runBlocking {
        val databaseName = "primal_mute_persist_${counter++}.db"
        LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
        val database = LocalDatabaseFactory.createDatabase<CachingDatabase>(databaseName = databaseName)
        val testDispatcher = UnconfinedTestDispatcher()
        val dispatcherProvider = mockk<DispatcherProvider> {
            every { io() } returns testDispatcher
            every { main() } returns testDispatcher
        }
        val settingsApi = mockk<SettingsApi>()
        val repository = MutedItemRepositoryImpl(
            dispatcherProvider = dispatcherProvider,
            database = database,
            settingsApi = settingsApi,
            primalPublisher = mockk<PrimalPublisher>(),
        )
        try {
            block(database, repository, settingsApi)
        } finally {
            database.close()
            LocalDatabaseFactory.deleteDatabases(names = listOf(databaseName))
        }
    }

    private fun muteListResponse(mutedPubkeys: List<String>) =
        GetMuteListResponse(
            muteList = NostrEvent(
                id = "mute-list-event-id",
                pubKey = USER_ID,
                createdAt = 1_700_000_000L,
                kind = NostrEventKind.MuteList.value,
                tags = mutedPubkeys.map { it.asPubkeyTag() },
                content = "",
                sig = "signature",
            ),
        )

    private fun mutedUser(pubkey: String) =
        MutedItemData(
            item = pubkey,
            ownerId = USER_ID,
            type = MutedItemType.User,
            listType = ListType.MuteList,
        )

    companion object {
        private const val USER_ID = "user-pubkey"
        private const val MUTED_A = "muted-pubkey-a"
        private const val MUTED_B = "muted-pubkey-b"

        private const val INVALIDATION_TIMEOUT_MS = 3_000L
        private const val NO_INVALIDATION_GRACE_MS = 800L

        private var counter = 0
    }
}
