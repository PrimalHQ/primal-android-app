package net.primal.android.networking.relays

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.UserAccount
import org.junit.Rule

@Suppress("LargeClass", "UnusedPrivateMember", "MaxLineLength", "ForbiddenComment")
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

    private fun buildActiveAccountStore() =
        mockk<ActiveAccountStore>(relaxed = true) {
            every { activeAccountState } returns flowOf(
                ActiveUserAccountState.ActiveUserAccount(
                    data = UserAccount
                        .buildLocal(pubkey = expectedUserId),
                ),
            )
            coEvery { activeUserId } returns MutableStateFlow(expectedUserId)
        }

    /* TODO: port this test */
//    @Test
//    fun `invalid relays does not cause the crash`() =
//        runTest {
//            RelaysSocketManager(
//                dispatchers = coroutinesTestRule.dispatcherProvider,
//                nostrSocketClientFactory = mockk(relaxed = true),
//                activeAccountStore = buildActiveAccountStore(),
//                primalDatabase = mockk(relaxed = true) {
//                    every { relays() } returns mockk(relaxed = true) {
//                        every { observeRelays(any()) } returns flowOf(
//                            invalidRelays.map {
//                                it.mapToRelayPO(userId = expectedUserId, kind = RelayKind.UserRelay)
//                            },
//                        )
//                    }
//                },
//                primalApiClient = mockk<PrimalApiClient>(relaxed = true),
//            )
//            advanceUntilIdleAndDelay()
//        }
}
