package net.primal.android.networking.primal

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.config.AppConfigHandler
import net.primal.android.config.FakeAppConfigProvider
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.networking.FakeWebSocketOkHttpClient
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.asWssUrl
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.networking.sockets.errors.WssException
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrimalApiClientTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val fakeAppConfigProvider = FakeAppConfigProvider()

    private fun buildPrimalApiClient(
        okHttpClient: OkHttpClient = spyk(),
        serverType: PrimalServerType = PrimalServerType.Caching,
        appConfigHandler: AppConfigHandler = mockk(relaxed = true),
    ): PrimalApiClient {
        return PrimalApiClient(
            okHttpClient = okHttpClient,
            serverType = serverType,
            appConfigProvider = fakeAppConfigProvider,
            appConfigHandler = appConfigHandler,
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
        )
    }

    @Test
    fun onInit_createsWebSocketConnection_UsingUrlFromAppConfigProvider() =
        runTest {
            val expectedCacheUrl = fakeAppConfigProvider.cacheUrl().value

            val spyOkHttpClient = spyk<FakeWebSocketOkHttpClient>()
            buildPrimalApiClient(okHttpClient = spyOkHttpClient)
            advanceUntilIdle()

            verify(exactly = 1) {
                spyOkHttpClient.newWebSocket(
                    withArg { it.asWssUrl() shouldBe expectedCacheUrl },
                    any(),
                )
            }
        }

    @Test
    fun whenUrlInAppConfigProviderChanges_recreatesWebSocketConnection() =
        runTest {
            val spyOkHttpClient = spyk<FakeWebSocketOkHttpClient>()
            buildPrimalApiClient(okHttpClient = spyOkHttpClient)
            advanceUntilIdle()

            val expectedFirstApiUrl = fakeAppConfigProvider.cacheUrl().value
            val expectedSecondApiUrl = "wss://new.primal.net/v1"
            fakeAppConfigProvider.setCacheUrl(expectedSecondApiUrl)
            advanceUntilIdle()

            verifyOrder {
                spyOkHttpClient.newWebSocket(
                    withArg { it.asWssUrl() shouldBe expectedFirstApiUrl },
                    any(),
                )

                spyOkHttpClient.newWebSocket(
                    withArg { it.asWssUrl() shouldBe expectedSecondApiUrl },
                    any(),
                )
            }
        }

    @Test
    fun whenWebSocketConnectionFails_appConfigUpdateIsExecuted() =
        runTest {
            val fakeOkHttpClient = FakeWebSocketOkHttpClient()
            val spyAppConfigHandler = mockk<AppConfigHandler>(relaxed = true)
            buildPrimalApiClient(okHttpClient = fakeOkHttpClient, appConfigHandler = spyAppConfigHandler)
            advanceUntilIdle()

            fakeOkHttpClient.failWebSocketConnection()
            advanceUntilIdle()

            coVerify(exactly = 1) {
                spyAppConfigHandler.updateAppConfigWithDebounce(any())
            }
        }

    @Test
    fun whenOpeningWebSocketConnection_properUserAgentIsInHeader() =
        runTest {
            val fakeOkHttpClient = spyk<FakeWebSocketOkHttpClient>()
            buildPrimalApiClient(okHttpClient = fakeOkHttpClient)
            advanceUntilIdle()

            verify {
                fakeOkHttpClient.newWebSocket(
                    withArg {
                        val uaHeader = it.header("User-Agent")
                        uaHeader.shouldNotBeNull()
                        uaHeader shouldBe UserAgentProvider.USER_AGENT
                    },
                    any(),
                )
            }
        }

    @Test
    fun queryRetriesRequestIfRequestFails() =
        runTest {
            val fakeOkHttpClient = FakeWebSocketOkHttpClient()
            val primalClient = buildPrimalApiClient(okHttpClient = fakeOkHttpClient)
            advanceUntilIdle()
            val mockNostrSocketClient = mockk<NostrSocketClient>(relaxed = true)
            primalClient.socketClient = mockNostrSocketClient

            try {
                primalClient.query(message = PrimalCacheFilter(primalVerb = PrimalVerb.IMPORT_EVENTS))
            } catch (_: WssException) {}

            verify(exactly = 1 + PrimalApiClient.MAX_RETRIES) {
                mockNostrSocketClient.sendREQ(any(), any())
            }
        }
}
