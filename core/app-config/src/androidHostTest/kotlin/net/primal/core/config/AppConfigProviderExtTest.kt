package net.primal.core.config

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import net.primal.core.coroutines.CoroutinesTestRule
import net.primal.domain.global.PrimalServerType
import org.junit.Rule
import org.junit.Test

class AppConfigProviderExtTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val fakeAppConfigProvider = FakeAppConfigProvider()

    @Test
    fun cacheUrl_matchesCachingServerType() =
        runTest {
            val expectedUrl = "wss://cache.primal.net"
            fakeAppConfigProvider.setCacheUrl(expectedUrl)
            fakeAppConfigProvider.observeApiUrlByType(type = PrimalServerType.Caching).value shouldBe expectedUrl
        }

    @Test
    fun uploadUrl_matchesUploadServerType() =
        runTest {
            val expectedUrl = "wss://upload.primal.net"
            fakeAppConfigProvider.setUploadUrl(expectedUrl)
            fakeAppConfigProvider.observeApiUrlByType(type = PrimalServerType.Upload).value shouldBe expectedUrl
        }

    @Test
    fun walletUrl_matchesWalletServerType() =
        runTest {
            val expectedUrl = "wss://wallet.primal.net"
            fakeAppConfigProvider.setWalletUrl(expectedUrl)
            fakeAppConfigProvider.observeApiUrlByType(type = PrimalServerType.Wallet).value shouldBe expectedUrl
        }
}
