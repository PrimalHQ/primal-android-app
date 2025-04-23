package net.primal.networking.primal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.primal.core.coroutines.CoroutinesTestRule
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
class PrimalApiClientTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    // TODO Fix PrimalApiClientTest

//    private val fakeAppConfigProvider = FakeAppConfigProvider()

//    private fun buildPrimalApiClient(
//        httpClient: HttpClient = spyk(),
//        serverType: PrimalServerType = PrimalServerType.Caching,
//        appConfigHandler: AppConfigHandler = mockk(relaxed = true),
//    ): PrimalApiClient {
//        return PrimalApiClientImpl(
//            httpClient = httpClient,
//            serverType = serverType,
//            appConfigProvider = fakeAppConfigProvider,
//            appConfigHandler = appConfigHandler,
//            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
//        )
//    }
//
//    @Test
//    fun onInit_createsWebSocketConnection_UsingUrlFromAppConfigProvider() =
//        runTest {
//            val expectedCacheUrl = fakeAppConfigProvider.cacheUrl().value
//
//            val spyOkHttpClient = spyk<FakeWebSocketOkHttpClient>()
//            buildPrimalApiClient(httpClient = spyOkHttpClient)
//            advanceUntilIdle()
//
//            verify(exactly = 1) {
//                spyOkHttpClient.newWebSocket(
//                    withArg { it.asWssUrl() shouldBe expectedCacheUrl },
//                    any(),
//                )
//            }
//        }
//
//    @Test
//    fun whenUrlInAppConfigProviderChanges_recreatesWebSocketConnection() =
//        runTest {
//            val spyOkHttpClient = spyk<FakeWebSocketOkHttpClient>()
//            buildPrimalApiClient(httpClient = spyOkHttpClient)
//            advanceUntilIdle()
//
//            val expectedFirstApiUrl = fakeAppConfigProvider.cacheUrl().value
//            val expectedSecondApiUrl = "wss://new.primal.net/v1"
//            fakeAppConfigProvider.setCacheUrl(expectedSecondApiUrl)
//            advanceUntilIdle()
//
//            verifyOrder {
//                spyOkHttpClient.newWebSocket(
//                    withArg { it.asWssUrl() shouldBe expectedFirstApiUrl },
//                    any(),
//                )
//
//                spyOkHttpClient.newWebSocket(
//                    withArg { it.asWssUrl() shouldBe expectedSecondApiUrl },
//                    any(),
//                )
//            }
//        }
//
//    @Test
//    fun whenWebSocketConnectionFails_appConfigUpdateIsExecuted() =
//        runTest {
//            val fakeOkHttpClient = FakeWebSocketOkHttpClient()
//            val spyAppConfigHandler = mockk<AppConfigHandler>(relaxed = true)
//            buildPrimalApiClient(httpClient = fakeOkHttpClient, appConfigHandler = spyAppConfigHandler)
//            advanceUntilIdle()
//
//            fakeOkHttpClient.failWebSocketConnection()
//            advanceUntilIdle()
//
//            coVerify(exactly = 1) {
//                spyAppConfigHandler.updateAppConfigWithDebounce(any())
//            }
//        }
//
//    @Test
//    fun whenOpeningWebSocketConnection_properUserAgentIsInHeader() =
//        runTest {
//            val fakeOkHttpClient = spyk<FakeWebSocketOkHttpClient>()
//            buildPrimalApiClient(httpClient = fakeOkHttpClient)
//            advanceUntilIdle()
//
//            verify {
//                fakeOkHttpClient.newWebSocket(
//                    withArg {
//                        val uaHeader = it.header("User-Agent")
//                        uaHeader.shouldNotBeNull()
//                        uaHeader shouldBe UserAgentProvider.USER_AGENT
//                    },
//                    any(),
//                )
//            }
//        }
//
//    @Test
//    fun queryRetriesRequestIfRequestFails() =
//        runTest {
//            val fakeOkHttpClient = FakeWebSocketOkHttpClient()
//            val primalClient = buildPrimalApiClient(httpClient = fakeOkHttpClient)
//            advanceUntilIdle()
//            val mockNostrSocketClient = mockk<NostrSocketClient>(relaxed = true)
//            primalClient.socketClient = mockNostrSocketClient
//
//            try {
//                primalClient.query(message = PrimalCacheFilter(primalVerb = PrimalVerb.IMPORT_EVENTS))
//            } catch (_: NetworkException) {}
//
//            verify(exactly = 1 + PrimalApiClient.MAX_RETRIES) {
//                mockNostrSocketClient.sendREQ(any(), any())
//            }
//        }
}
