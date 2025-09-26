package net.primal.core.config

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import net.primal.core.config.api.ApiConfigResponse
import net.primal.core.config.api.WellKnownApi
import net.primal.core.config.store.AppConfigDataStore
import net.primal.core.coroutines.CoroutinesTestRule
import org.junit.Rule
import org.junit.Test

class AppConfigHandlerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    @Test
    fun whenUpdateWithDebounceIsCalledMultipleTimesWithinDebounceValue_fetchIsPerformedOnlyOnce() =
        runTest {
            val wellKnownApi = mockk<WellKnownApi>(relaxed = true)
            val appConfigHandler = AppConfigHandler(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                appConfigStore = mockk(relaxed = true),
                wellKnownApi = wellKnownApi,
            )

            val debounceTimeout = 1.minutes

            appConfigHandler.updateWithDebounce(debounceTimeout)
            appConfigHandler.updateWithDebounce(debounceTimeout)
            appConfigHandler.updateWithDebounce(debounceTimeout)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                wellKnownApi.fetchApiConfig()
            }
        }

    @Test
    fun whenUpdateAppConfigFails_itDoesNotThrowException() =
        runTest {
            val wellKnownApi = mockk<WellKnownApi>(relaxed = true) {
                coEvery { fetchApiConfig() } throws IOException()
                // TODO This needs to be ported properly
//                    mockk<Response<String>>(
//                        relaxed = true
//                    )
//                )
            }
            val appConfigHandler = AppConfigHandler(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                appConfigStore = mockk(relaxed = true),
                wellKnownApi = wellKnownApi,
            )

            appConfigHandler.updateImmediately()
        }

    @Test
    fun whenUpdateAppConfigSucceeds_itUpdatesEndpointValuesInDataStore() =
        runTest {
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

            val appConfigPersistence = FakeDataStore(initialValue = DEFAULT_APP_CONFIG)

            val appConfigHandler = AppConfigHandler(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                appConfigStore = AppConfigDataStore(
                    dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                    persistence = appConfigPersistence,
                ),
                wellKnownApi = wellKnownApi,
            )

            appConfigHandler.updateImmediately()
            advanceUntilIdle()

            appConfigPersistence.latestData.cacheUrl shouldBe expectedTestCacheUrl
            appConfigPersistence.latestData.uploadUrl shouldBe expectedTestUploadUrl
            appConfigPersistence.latestData.walletUrl shouldBe expectedTestWalletUrl
        }

    @Test
    fun overrideCacheUrl_callsOverrideCacheUrlOnAppConfigStore_withCorrectUrl() =
        runTest {
            val appConfigStore = mockk<AppConfigDataStore>(relaxed = true)
            val appConfigHandler = AppConfigHandler(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                appConfigStore = appConfigStore,
                wellKnownApi = mockk<WellKnownApi>(relaxed = true),
            )

            val expectCacheOverrideUrl = "cacheOverride"

            appConfigHandler.overrideCacheUrl(url = expectCacheOverrideUrl)
            advanceUntilIdle()

            coVerify {
                appConfigStore.overrideCacheUrl(
                    withArg { arg ->
                        arg shouldBe expectCacheOverrideUrl
                    },
                )
            }
        }

    @Test
    fun restoreDefaultCacheUrl_callsRevertCacheUrlOverrideFlag() =
        runTest {
            val appConfigStore = mockk<AppConfigDataStore>(relaxed = true)
            val appConfigHandler = AppConfigHandler(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                appConfigStore = appConfigStore,
                wellKnownApi = mockk<WellKnownApi>(relaxed = true),
            )
            appConfigStore.overrideCacheUrl(url = "cacheOverride")
            appConfigHandler.restoreDefaultCacheUrl()
            advanceUntilIdle()

            coVerify {
                appConfigStore.revertCacheUrlOverrideFlag()
            }
        }

    @Test
    fun restoreDefaultCacheUrl_updatesCacheUrl_withWellKnownCacheUrlInDataStore() =
        runTest {
            val expectedWellKnownCacheUrl = "well-known-cache-url"
            val wellKnownApi = mockk<WellKnownApi>(relaxed = true) {
                coEvery { fetchApiConfig() } returns ApiConfigResponse(
                    cacheServers = listOf(expectedWellKnownCacheUrl),
                    walletServers = emptyList(),
                    uploadServers = emptyList(),
                )
            }

            val appConfigPersistence = FakeDataStore(initialValue = DEFAULT_APP_CONFIG)
            val appConfigHandler = AppConfigHandler(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                appConfigStore = AppConfigDataStore(
                    dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                    persistence = appConfigPersistence,
                ),
                wellKnownApi = wellKnownApi,
            )

            appConfigHandler.restoreDefaultCacheUrl()
            advanceUntilIdle()

            appConfigPersistence.latestData.cacheUrl shouldBe expectedWellKnownCacheUrl
        }

    @Test
    fun restoreDefaultCacheUrl_updatesCacheUrl_withDefaultCacheUrlInDataStore_ifWellKnownCallFails() =
        runTest {
            val wellKnownApi = mockk<WellKnownApi>(relaxed = true) {
                coEvery { fetchApiConfig() } throws IOException()
            }
            val appConfigPersistence = FakeDataStore(initialValue = DEFAULT_APP_CONFIG.copy(cacheUrl = "fake"))
            val appConfigHandler = AppConfigHandler(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                appConfigStore = AppConfigDataStore(
                    dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                    persistence = appConfigPersistence,
                ),
                wellKnownApi = wellKnownApi,
            )

            appConfigHandler.restoreDefaultCacheUrl()
            advanceUntilIdle()

            appConfigPersistence.latestData.cacheUrl shouldBe DEFAULT_APP_CONFIG.cacheUrl
        }
}
