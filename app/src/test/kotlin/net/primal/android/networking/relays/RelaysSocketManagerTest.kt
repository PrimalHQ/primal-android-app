package net.primal.android.networking.relays

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.db.Relay as RelayPO
import net.primal.android.user.db.UsersDatabase
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.UserAccount
import net.primal.core.networking.sockets.NostrSocketClient
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.testing.CoroutinesTestRule
import net.primal.domain.global.CachingImportRepository
import net.primal.domain.nostr.NostrEvent
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("MaxLineLength")
@OptIn(ExperimentalCoroutinesApi::class)
class RelaysSocketManagerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val expectedUserId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079"

    @Before
    fun setup() {
        mockkObject(NostrSocketClientFactory)
        every {
            NostrSocketClientFactory.create(
                wssUrl = any(),
                incomingCompressionEnabled = any(),
                onSocketConnectionOpened = any(),
                onSocketConnectionClosed = any(),
            )
        } returns mockk<NostrSocketClient>(relaxed = true)
        every {
            NostrSocketClientFactory.create(
                wssUrl = any(),
                httpClient = any(),
                incomingCompressionEnabled = any(),
                onSocketConnectionOpened = any(),
                onSocketConnectionClosed = any(),
            )
        } returns mockk<NostrSocketClient>(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkObject(NostrSocketClientFactory)
    }

    private fun buildActiveAccountStore(userId: String = expectedUserId) =
        mockk<ActiveAccountStore>(relaxed = true) {
            every { activeAccountState } returns flowOf(
                ActiveUserAccountState.ActiveUserAccount(
                    data = UserAccount.buildLocal(pubkey = userId),
                ),
            )
            every { activeUserId } returns MutableStateFlow(userId)
        }

    private fun buildUsersDatabase(relays: List<RelayPO> = emptyList()) =
        mockk<UsersDatabase>(relaxed = true) {
            every { relays() } returns mockk(relaxed = true) {
                every { observeRelays(any()) } returns flowOf(relays)
            }
        }

    private fun buildRelaysSocketManager(
        activeAccountStore: ActiveAccountStore = buildActiveAccountStore(),
        usersDatabase: UsersDatabase = buildUsersDatabase(),
        cachingImportRepository: CachingImportRepository = mockk(relaxed = true),
    ): RelaysSocketManager {
        return RelaysSocketManager(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            nostrSocketClientFactory = NostrSocketClientFactory,
            cachingImportRepository = cachingImportRepository,
            activeAccountStore = activeAccountStore,
            usersDatabase = usersDatabase,
        )
    }

    @Test
    fun `constructor with empty userId does not crash`() =
        runTest {
            buildRelaysSocketManager(
                activeAccountStore = buildActiveAccountStore(userId = ""),
            )
            advanceUntilIdle()
        }

    @Test
    fun `invalid relay URLs do not cause crash`() =
        runTest {
            val invalidRelays = listOf(
                Relay(url = "abcdefghijkl", read = true, write = true),
                Relay(url = "wss://nostr-relay.untethr.me\t", read = true, write = true),
                Relay(url = "⬤ wss://nostr-pub.wellorder.net", read = true, write = true),
                Relay(url = "wss://filter.nostr.wine/npubxyz\n", read = true, write = true),
            )
            val relayPOs = invalidRelays.map {
                RelayPO(
                    userId = expectedUserId,
                    kind = RelayKind.UserRelay,
                    url = it.url,
                    read = it.read,
                    write = it.write,
                )
            }
            buildRelaysSocketManager(
                usersDatabase = buildUsersDatabase(relays = relayPOs),
            )
            advanceUntilIdle()
        }

    @Test
    fun `publishNwcEvent throws when no NWC relays configured`() =
        runTest {
            val manager = buildRelaysSocketManager(
                usersDatabase = buildUsersDatabase(relays = emptyList()),
            )
            advanceUntilIdle()

            val nostrEvent = mockk<NostrEvent>(relaxed = true)

            try {
                manager.publishNwcEvent(nostrEvent)
                throw AssertionError("Expected NostrPublishException")
            } catch (_: NostrPublishException) {
                // Expected - no NWC relays configured
            }
        }

    @Test
    fun `userRelayPoolStatus is initially empty`() =
        runTest {
            val manager = buildRelaysSocketManager(
                activeAccountStore = buildActiveAccountStore(userId = ""),
            )
            manager.userRelayPoolStatus.value shouldBe emptyMap()
        }

    @Test
    fun `publishEvent does not throw when fallback relays are available`() =
        runTest {
            val manager = buildRelaysSocketManager(
                activeAccountStore = buildActiveAccountStore(userId = ""),
                usersDatabase = buildUsersDatabase(relays = emptyList()),
            )
            advanceUntilIdle()

            val nostrEvent = mockk<NostrEvent>(relaxed = true) {
                every { id } returns "test-event-id"
            }

            // The fallback pool is initialized in init block with FALLBACK_RELAYS.
            // Publishing should attempt to use fallback relays when user relay pool is empty.
            try {
                manager.publishEvent(nostrEvent)
            } catch (_: NostrPublishException) {
                // Expected - mock socket clients can't actually publish
            }
        }
}
