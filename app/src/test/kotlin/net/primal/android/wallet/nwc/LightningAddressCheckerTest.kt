package net.primal.android.wallet.nwc

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.primal.android.core.coroutines.CoroutinesTestRule
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LightningAddressCheckerTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private fun buildMockOkHttpClient(responseCode: Int): OkHttpClient {
        val response = mockk<Response>(relaxed = true) {
            every { code } returns responseCode
        }
        val mockCall = mockk<Call> {
            every { execute() } returns response
        }
        val okHttpClient = mockk<OkHttpClient> {
            every { newCall(any()) } returns mockCall
        }
        return okHttpClient
    }

    @Test
    fun validateLightningAddress_doesNotRaiseExceptionForValidAddress() =
        runTest {
            val checker = LightningAddressChecker(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                okHttpClient = buildMockOkHttpClient(responseCode = 200),
            )
            checker.validateLightningAddress("alex@primal.net")
        }

    @Test(expected = InvalidLud16Exception::class)
    fun validateLightningAddress_throwsExceptionForInvalidAddress() =
        runTest {
            val checker = LightningAddressChecker(
                dispatcherProvider = coroutinesTestRule.dispatcherProvider,
                okHttpClient = buildMockOkHttpClient(responseCode = 404),
            )
            checker.validateLightningAddress("invalidaddress123$^@primal.net")
        }
}
