package net.primal.core.config.store

import net.primal.core.coroutines.CoroutinesTestRule
import org.junit.Rule
import org.junit.Test

class AppConfigDataStoreTest {

    companion object {
        private const val DATA_STORE_FILE = "appConfigDataStore.json"
    }

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    // TODO Migrate tests to work within core:app-config module

    @Test
    fun fixTests() = Unit

//    private val testContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
//
//    private val persistence: DataStore<AppConfig> =
//        createDataStore { testContext.dataStoreFile(DATA_STORE_FILE).path }
//
//    private fun createAppConfigDataStore(
//        dispatcherProvider: DispatcherProvider = coroutinesTestRule.dispatcherProvider,
//        persistenceDataStore: DataStore<AppConfig> = persistence,
//    ): AppConfigDataStore {
//        return AppConfigDataStore(
//            dispatcherProvider = dispatcherProvider,
//            persistence = persistenceDataStore,
//        )
//    }
//
//    @Test
//    fun updateConfig_updatesAllFieldsWhenOverrideIsFalse() =
//        runTest {
//            val store = createAppConfigDataStore()
//
//            val expectedWalletUrl = "walletUrl"
//            val expectedUploadUrl = "uploadUrl"
//            val expectedCacheUrl = "cacheUrl"
//
//            val actual = store.updateConfig {
//                copy(
//                    cacheUrl = expectedCacheUrl,
//                    walletUrl = expectedWalletUrl,
//                    uploadUrl = expectedUploadUrl,
//                )
//            }
//
//            actual.cacheUrl shouldBe expectedCacheUrl
//            actual.walletUrl shouldBe expectedWalletUrl
//            actual.uploadUrl shouldBe expectedUploadUrl
//        }
//
//    @Test
//    fun updateConfig_respectsCacheOverrideWhenUpdating() =
//        runTest {
//            val store = createAppConfigDataStore()
//            val expectedOverriddenCacheUrl = "cacheOverride"
//            store.updateConfig {
//                copy(
//                    cacheUrlOverride = true,
//                    cacheUrl = expectedOverriddenCacheUrl,
//                )
//            }
//            val expectedUploadUrl = "uploadUrl"
//            val expectedWalletUrl = "walletUrl"
//
//            val actual = store.updateConfig {
//                copy(
//                    cacheUrl = "thisShouldNotBeUpdated",
//                    walletUrl = expectedWalletUrl,
//                    uploadUrl = expectedUploadUrl,
//                )
//            }
//
//            actual.uploadUrl shouldBe expectedUploadUrl
//            actual.walletUrl shouldBe expectedWalletUrl
//            actual.cacheUrlOverride shouldBe true
//            actual.cacheUrl shouldBe expectedOverriddenCacheUrl
//        }
//
//    @Test
//    fun overrideCacheUrl_setsOverrideFlag() =
//        runTest {
//            val store = createAppConfigDataStore()
//
//            store.overrideCacheUrl(url = "cacheOverride")
//            advanceUntilIdle()
//
//            val actual = store.config.value
//            actual.cacheUrlOverride shouldBe true
//        }
//
//    @Test
//    fun overrideCacheUrl_doesNotUpdateOtherUrls() =
//        runTest {
//            val store = createAppConfigDataStore()
//
//            val initial = store.config.value
//            store.overrideCacheUrl(url = "cacheOverride")
//            advanceUntilIdle()
//
//            val actual = store.config.value
//            actual.uploadUrl shouldBe initial.uploadUrl
//            actual.walletUrl shouldBe initial.walletUrl
//        }
//
//    @Test
//    fun overrideCacheUrl_setsGivenCacheUrl() =
//        runTest {
//            val store = createAppConfigDataStore()
//            val expectedOverriddenCacheUrl = "cacheOverride"
//
//            store.overrideCacheUrl(url = expectedOverriddenCacheUrl)
//            advanceUntilIdle()
//
//            val actual = store.config.value
//            actual.cacheUrl shouldBe expectedOverriddenCacheUrl
//        }
//
//    @Test
//    fun revertCacheUrlOverrideFlag_disabledOverrideFlag() =
//        runTest {
//            val store = createAppConfigDataStore()
//            store.updateConfig {
//                copy(
//                    cacheUrlOverride = true,
//                    cacheUrl = "overridden",
//                )
//            }
//            store.revertCacheUrlOverrideFlag()
//            advanceUntilIdle()
//
//            val actual = store.config.value
//            actual.cacheUrlOverride shouldBe false
//        }
}
