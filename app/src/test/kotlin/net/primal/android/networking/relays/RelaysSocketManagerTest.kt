package net.primal.android.networking.relays

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.core.advanceUntilIdleAndDelay
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.mapToRelayPO
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RelaysSocketManagerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val expectedUserId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079"

    private val invalidRelays = listOf(
        Relay(url = "abcdefghijkl", true, true),
        Relay(url = "wss://nostr-relay.untethr.me\t", true, true),
        Relay(url = "⬤ wss://nostr-pub.wellorder.net", true, true),
        Relay(url = "wss://filter.nostr.wine/npubxyz\n", true, true),
    )

    private fun buildActiveAccountStore() = mockk<ActiveAccountStore>(relaxed = true) {
        every { activeAccountState } returns flowOf(
            ActiveUserAccountState.ActiveUserAccount(
                data = UserAccount
                    .buildLocal(pubkey = expectedUserId),
            ),
        )
    }

    @Test
    fun `invalid relays does not cause the crash`() = runTest {
        RelaysSocketManager(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            activeAccountStore = buildActiveAccountStore(),
            primalDatabase = mockk(relaxed = true) {
                every { relays() } returns mockk(relaxed = true) {
                    every { observeRelays(any()) } returns flowOf(
                    invalidRelays.map {
                        it.mapToRelayPO(userId = expectedUserId, kind = RelayKind.UserRelay)
                    },
                )
                }
            },
            userRelaysPool = RelayPool(
                dispatchers = coroutinesTestRule.dispatcherProvider,
                okHttpClient = mockk<OkHttpClient>(),
            ),
            nwcRelaysPool = RelayPool(
                dispatchers = coroutinesTestRule.dispatcherProvider,
                okHttpClient = mockk<OkHttpClient>(),
            ),
            bootstrapRelays = RelayPool(
                dispatchers = coroutinesTestRule.dispatcherProvider,
                okHttpClient = mockk<OkHttpClient>(),
            ),
        )
        advanceUntilIdleAndDelay()
    }
}
