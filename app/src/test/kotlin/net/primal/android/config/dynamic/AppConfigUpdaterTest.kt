package net.primal.android.config.dynamic

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.android.config.api.ApiConfigResponse
import net.primal.android.config.api.WellKnownApi
import net.primal.android.config.store.AppConfigDataStore
import net.primal.android.config.store.FakeAppConfigStore
import net.primal.android.core.coroutines.CoroutinesTestRule
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AppConfigUpdaterTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @Test
    fun whenUpdateWithDebounceIsCalledMultipleTimesWithinDebounceValue_fetchIsPerformedOnlyOnce() = runTest {
        val wellKnownApi = mockk<WellKnownApi>(relaxed = true)
        val appConfigUpdater = AppConfigUpdater(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            appConfigStore = mockk(relaxed = true),
            wellKnownApi = wellKnownApi,
        )

        val debounceTimeout = 1.minutes

        appConfigUpdater.updateAppConfigWithDebounce(debounceTimeout)
        appConfigUpdater.updateAppConfigWithDebounce(debounceTimeout)
        appConfigUpdater.updateAppConfigWithDebounce(debounceTimeout)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            wellKnownApi.fetchApiConfig()
        }
    }

    @Test
    fun whenUpdateAppConfigFails_itDoesNotThrowException() = runTest {
        val wellKnownApi = mockk<WellKnownApi>(relaxed = true) {
            coEvery { fetchApiConfig() } throws retrofit2.HttpException(mockk<Response<String>>(relaxed = true))
        }
        val appConfigUpdater = AppConfigUpdater(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            appConfigStore = mockk(relaxed = true),
            wellKnownApi = wellKnownApi,
        )

        appConfigUpdater.updateAppConfigOrFailSilently()
    }

    @Test
    fun whenUpdateAppConfigSucceeds_itUpdatesEndpointValuesInDataStore() = runTest {
        val expectedTestCacheUrl = "wss://testCache.primal.net"
        val expectedTestUploadUrl = "wss://testUpload.primal.net"
        val expectedTestWalletUrl = "wss://testWallet.primal.net"
        val wellKnownApi = mockk<WellKnownApi>(relaxed = true) {
            coEvery { fetchApiConfig() } returns ApiConfigResponse(
                cacheServers = listOf(expectedTestCacheUrl),
                uploadServers = listOf(expectedTestUploadUrl),
                walletServers = listOf(expectedTestWalletUrl),
            )
        }

        val appConfigPersistence = FakeAppConfigStore()

        val appConfigUpdater = AppConfigUpdater(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            appConfigStore = AppConfigDataStore(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                persistence = appConfigPersistence,
            ),
            wellKnownApi = wellKnownApi,
        )

        appConfigUpdater.updateAppConfigOrFailSilently()
        advanceUntilIdle()

        appConfigPersistence.latestAppConfig.cacheUrl shouldBe expectedTestCacheUrl
        appConfigPersistence.latestAppConfig.uploadUrl shouldBe expectedTestUploadUrl
        appConfigPersistence.latestAppConfig.walletUrl shouldBe expectedTestWalletUrl
    }
}
