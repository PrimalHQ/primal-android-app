package net.primal.networking.primal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.primal.core.networking.primal.BasePrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClientImpl
import net.primal.core.testing.CoroutinesTestRule
import net.primal.domain.common.exception.NetworkException
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrimalApiClientTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val incomingMessages = MutableSharedFlow<NostrIncomingMessage>()

    private fun buildMockSocketClient(sendREQShouldFail: Boolean = false): NostrSocketClientImpl {
        val mockSocket = mockk<NostrSocketClientImpl>(relaxed = true)
        every { mockSocket.incomingMessages } returns incomingMessages.asSharedFlow()
        if (sendREQShouldFail) {
            coEvery { mockSocket.sendREQ(any(), any()) } throws RuntimeException("Send failed")
        }
        return mockSocket
    }

    private fun buildPrimalApiClient(
        socketClient: NostrSocketClientImpl = buildMockSocketClient(),
    ): BasePrimalApiClient {
        return BasePrimalApiClient(socketClient = socketClient)
    }

    @Test
    fun query_sendsREQToSocketClient() =
        runTest {
            val mockSocketClient = buildMockSocketClient()
            val apiClient = buildPrimalApiClient(socketClient = mockSocketClient)

            try {
                apiClient.query(message = PrimalCacheFilter(primalVerb = "test_verb"))
            } catch (_: Exception) {
                // Expected - query times out because no EOSE is sent back
            }

            coVerify { mockSocketClient.sendREQ(any(), any()) }
        }

    @Test
    fun query_whenSendFails_throwsNetworkException() =
        runTest {
            val mockSocketClient = buildMockSocketClient(sendREQShouldFail = true)
            val apiClient = buildPrimalApiClient(socketClient = mockSocketClient)

            shouldThrow<NetworkException> {
                apiClient.query(message = PrimalCacheFilter(primalVerb = "test_verb"))
            }
        }

    @Test
    fun subscribe_sendsREQAndReturnsFlow() =
        runTest {
            val mockSocketClient = buildMockSocketClient()
            val apiClient = buildPrimalApiClient(socketClient = mockSocketClient)

            val flow = apiClient.subscribe(
                subscriptionId = "test-sub-id",
                message = PrimalCacheFilter(primalVerb = "test_verb"),
            )

            val job = launch {
                flow.collect { }
            }
            advanceUntilIdle()

            coVerify { mockSocketClient.sendREQ(eq("test-sub-id"), any()) }
            job.cancel()
        }

    @Test
    fun closeSubscription_sendsCLOSEToSocketClient() =
        runTest {
            val mockSocketClient = buildMockSocketClient()
            val apiClient = buildPrimalApiClient(socketClient = mockSocketClient)

            val result = apiClient.closeSubscription(subscriptionId = "test-sub-id")

            result shouldBe true
            coVerify { mockSocketClient.sendCLOSE(eq("test-sub-id")) }
        }

    @Test
    fun closeSubscription_whenCLOSEFails_returnsFalse() =
        runTest {
            val mockSocketClient = buildMockSocketClient()
            coEvery { mockSocketClient.sendCLOSE(any()) } throws RuntimeException("Close failed")
            val apiClient = buildPrimalApiClient(socketClient = mockSocketClient)

            val result = apiClient.closeSubscription(subscriptionId = "test-sub-id")

            result shouldBe false
        }
}
