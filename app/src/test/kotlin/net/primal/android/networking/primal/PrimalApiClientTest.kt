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
import net.primal.android.config.FakeAppConfigProvider
import net.primal.android.config.dynamic.AppConfigUpdater
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.networking.FakeWebSocketOkHttpClient
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.asWssUrl
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
        appConfigUpdater: AppConfigUpdater = mockk(relaxed = true),
    ): PrimalApiClient {
        return PrimalApiClient(
            okHttpClient = okHttpClient,
            serverType = serverType,
            appConfigProvider = fakeAppConfigProvider,
            appConfigUpdater = appConfigUpdater,
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
        )
    }

    @Test
    fun onInit_createsWebSocketConnection_UsingUrlFromAppConfigProvider() = runTest {
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
    fun whenUrlInAppConfigProviderChanges_recreatesWebSocketConnection() = runTest {
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
    fun whenWebSocketConnectionFails_appConfigUpdateIsExecuted() = runTest {
        val fakeOkHttpClient = FakeWebSocketOkHttpClient()
        val spyAppConfigUpdater = mockk<AppConfigUpdater>(relaxed = true)
        buildPrimalApiClient(okHttpClient = fakeOkHttpClient, appConfigUpdater = spyAppConfigUpdater)
        advanceUntilIdle()

        fakeOkHttpClient.failWebSocketConnection()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            spyAppConfigUpdater.updateAppConfigWithDebounce(any())
        }
    }

    @Test
    fun whenOpeningWebSocketConnection_properUserAgentIsInHeader() = runTest {
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
}
