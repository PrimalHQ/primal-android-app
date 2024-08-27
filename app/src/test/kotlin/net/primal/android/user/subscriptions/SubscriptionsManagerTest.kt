package net.primal.android.user.subscriptions

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import net.primal.android.config.AppConfigHandler
import net.primal.android.config.AppConfigProvider
import net.primal.android.config.FakeAppConfigProvider
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalServerType
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.domain.UserAccount
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(value = AndroidJUnit4::class)
class SubscriptionsManagerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val activeAccountStoreMock = mockk<ActiveAccountStore>(relaxed = true) {
        every { activeAccountState } returns flow {
            emit(ActiveUserAccountState.ActiveUserAccount(data = UserAccount.EMPTY))
        }
    }

    private val emptyNostrEvent = NostrEvent(
        content = "",
        pubKey = "",
        createdAt = 0L,
        id = "",
        kind = NostrEventKind.WalletRequest.value,
        sig = "",
    )

    private fun buildSubscriptionsManager(
        activeAccountStore: ActiveAccountStore = mockk(relaxed = true),
        appConfigProvider: AppConfigProvider = mockk(relaxed = true),
        cacheApiClient: PrimalApiClient = mockk(relaxed = true),
        walletApiClient: PrimalApiClient = mockk(relaxed = true),
    ): SubscriptionsManager {
        return SubscriptionsManager(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            activeAccountStore = activeAccountStore,
            userRepository = mockk(relaxed = true),
            nostrNotary = mockk(relaxed = true) {
                every { signPrimalWalletOperationNostrEvent(any(), any()) } returns emptyNostrEvent
            },
            appConfigProvider = appConfigProvider,
            cacheApiClient = cacheApiClient,
            walletApiClient = walletApiClient,
        )
    }

    private fun buildPrimalApiClient(
        okHttpClient: OkHttpClient = spyk(),
        serverType: PrimalServerType = PrimalServerType.Caching,
        appConfigProvider: AppConfigProvider = mockk(relaxed = true),
        appConfigHandler: AppConfigHandler = mockk(relaxed = true),
    ): PrimalApiClient {
        return PrimalApiClient(
            okHttpClient = okHttpClient,
            serverType = serverType,
            appConfigProvider = appConfigProvider,
            appConfigHandler = appConfigHandler,
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
        )
    }

    @Test
    fun initializesSuccessfully_immediateAppConfigProvider() =
        runTest {
            val fakeAppConfigProvider = FakeAppConfigProvider()
            buildSubscriptionsManager(
                activeAccountStore = activeAccountStoreMock,
                appConfigProvider = fakeAppConfigProvider,
                cacheApiClient = buildPrimalApiClient(
                    serverType = PrimalServerType.Caching,
                    appConfigProvider = FakeAppConfigProvider(),
                ),
            )
        }

    @Test
    fun initializesSuccessfully_delayedAppConfigProvider() =
        runTest {
            val fakeAppConfigProvider = FakeAppConfigProvider(
                startDelay = 1_000L,
                delayDispatcher = coroutinesTestRule.dispatcherProvider.main(),
            )
            buildSubscriptionsManager(
                activeAccountStore = activeAccountStoreMock,
                appConfigProvider = fakeAppConfigProvider,
                cacheApiClient = buildPrimalApiClient(
                    serverType = PrimalServerType.Caching,
                    appConfigProvider = fakeAppConfigProvider,
                ),
            )
        }
}
