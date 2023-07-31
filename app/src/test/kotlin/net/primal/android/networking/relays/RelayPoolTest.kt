package net.primal.android.networking.relays

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.primal.android.test.advanceUntilIdleAndDelay
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.active.ActiveUserAccountState
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.UserAccount
import okhttp3.OkHttpClient
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RelayPoolTest {

    private fun buildActiveAccountStore(
        relays: List<Relay> = emptyList()
    ) = mockk<ActiveAccountStore>(relaxed = true) {
        every { activeAccountState } returns flowOf(
            ActiveUserAccountState.ActiveUserAccount(
                data = UserAccount
                    .buildLocal(pubkey = "")
                    .copy(relays = relays)
            )
        )
    }

    @Test
    fun `invalid relays does not cause the crash`() = runTest {
        RelayPool(
            okHttpClient = OkHttpClient(),
            activeAccountStore = buildActiveAccountStore(
                relays = listOf(
                    Relay(url = "abcdefghijkl", true, true),
                    Relay(url = "wss://nostr-relay.untethr.me\t", true, true),
                    Relay(url = "⬤ wss://nostr-pub.wellorder.net", true, true),
                    Relay(url = "wss://filter.nostr.wine/npubxyz\n", true, true),
                )
            )
        )
        advanceUntilIdleAndDelay()
    }

}
