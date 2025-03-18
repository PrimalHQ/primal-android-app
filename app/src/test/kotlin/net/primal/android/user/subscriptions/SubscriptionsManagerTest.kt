package net.primal.android.user.subscriptions

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith

// TODO Fix SubscriptionsManagerTest
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(value = AndroidJUnit4::class)
class SubscriptionsManagerTest {

    @Test
    fun weShouldFixThis() = Unit
//
//    @get:Rule
//    val coroutinesTestRule = CoroutinesTestRule()
//
//    private val activeAccountStoreMock = mockk<ActiveAccountStore>(relaxed = true) {
//        every { activeUserId } returns MutableStateFlow("")
//    }
//
//    private val emptyNostrEvent = NostrEvent(
//        content = "",
//        pubKey = "",
//        createdAt = 0L,
//        id = "",
//        kind = NostrEventKind.WalletRequest.value,
//        sig = "",
//    )
//
//    private fun buildSubscriptionsManager(
//        activeAccountStore: ActiveAccountStore = mockk(relaxed = true),
//        appConfigProvider: AppConfigProvider = mockk(relaxed = true),
//        cacheApiClient: PrimalApiClient = mockk(relaxed = true),
//        walletApiClient: PrimalApiClient = mockk(relaxed = true),
//    ): SubscriptionsManager {
//        return SubscriptionsManager(
//            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
//            activeAccountStore = activeAccountStore,
//            userRepository = mockk(relaxed = true),
//            nostrNotary = mockk(relaxed = true) {
//                every { signPrimalWalletOperationNostrEvent(any(), any()) } returns emptyNostrEvent
//            },
//            appConfigProvider = appConfigProvider,
//            cacheApiClient = cacheApiClient,
//            walletApiClient = walletApiClient,
//        )
//    }
//
//    private fun buildPrimalApiClient(
//        okHttpClient: OkHttpClient = spyk(),
//        serverType: PrimalServerType = PrimalServerType.Caching,
//        appConfigProvider: AppConfigProvider = mockk(relaxed = true),
//        appConfigHandler: AppConfigHandler = mockk(relaxed = true),
//    ): PrimalApiClient {
//        return PrimalApiClient(
//            okHttpClient = okHttpClient,
//            serverType = serverType,
//            appConfigProvider = appConfigProvider,
//            appConfigHandler = appConfigHandler,
//            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
//        )
//    }
//
//    @Test
//    fun initializesSuccessfully_immediateAppConfigProvider() =
//        runTest {
//            val fakeAppConfigProvider = FakeAppConfigProvider()
//            buildSubscriptionsManager(
//                activeAccountStore = activeAccountStoreMock,
//                appConfigProvider = fakeAppConfigProvider,
//                cacheApiClient = buildPrimalApiClient(
//                    serverType = PrimalServerType.Caching,
//                    appConfigProvider = FakeAppConfigProvider(),
//                ),
//            )
//        }
//
//    @Test
//    fun initializesSuccessfully_delayedAppConfigProvider() =
//        runTest {
//            val fakeAppConfigProvider = FakeAppConfigProvider(
//                startDelay = 1_000L,
//                delayDispatcher = coroutinesTestRule.dispatcherProvider.main(),
//            )
//            buildSubscriptionsManager(
//                activeAccountStore = activeAccountStoreMock,
//                appConfigProvider = fakeAppConfigProvider,
//                cacheApiClient = buildPrimalApiClient(
//                    serverType = PrimalServerType.Caching,
//                    appConfigProvider = fakeAppConfigProvider,
//                ),
//            )
//        }
}
