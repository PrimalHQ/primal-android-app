package net.primal.android.user.subscriptions

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.Badges
import net.primal.android.wallet.di.ActiveWalletBalanceSyncerFactory
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.testing.CoroutinesTestRule
import net.primal.domain.streams.StreamRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(value = AndroidJUnit4::class)
class SubscriptionsManagerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private fun buildActiveAccountStore(userId: String = "") =
        mockk<ActiveAccountStore>(relaxed = true) {
            every { activeUserId } returns MutableStateFlow(userId)
        }

    private fun buildSubscriptionsManager(
        activeAccountStore: ActiveAccountStore = buildActiveAccountStore(),
        streamRepository: StreamRepository = mockk(relaxed = true),
        cacheApiClient: PrimalApiClient = mockk(relaxed = true),
        activeWalletBalanceSyncerFactory: ActiveWalletBalanceSyncerFactory = mockk(relaxed = true),
    ): SubscriptionsManager {
        return SubscriptionsManager(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            activeAccountStore = activeAccountStore,
            streamRepository = streamRepository,
            cacheApiClient = cacheApiClient,
            activeWalletBalanceSyncerFactory = activeWalletBalanceSyncerFactory,
        )
    }

    @Test
    fun `construction with no active user does not crash`() {
        buildSubscriptionsManager(
            activeAccountStore = buildActiveAccountStore(userId = ""),
        )
    }

    @Test
    fun `badges flow is not null after construction`() {
        val manager = buildSubscriptionsManager()
        manager.badges shouldNotBe null
    }

    @Test
    fun `Badges default values are zero`() {
        val badges = Badges()
        badges.unreadNotificationsCount shouldBe 0
        badges.unreadMessagesCount shouldBe 0
    }
}
