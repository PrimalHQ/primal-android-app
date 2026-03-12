package net.primal.networking.sockets

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import net.primal.core.networking.sockets.NostrSocketClientImpl
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.testing.CoroutinesTestRule
import net.primal.domain.common.exception.NetworkException
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NostrSocketClientImplTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private val mockHttpClient = mockk<HttpClient>(relaxed = true)
    private val activeJob = Job()
    private val mockWebSocketSession = mockk<DefaultClientWebSocketSession>(relaxed = true) {
        every { coroutineContext } returns (activeJob as CoroutineContext)
    }

    @Before
    fun setUp() {
        mockkStatic("io.ktor.client.plugins.websocket.BuildersKt")
        coEvery {
            mockHttpClient.webSocketSession(urlString = any<String>())
        } returns mockWebSocketSession
    }

    @After
    fun tearDown() {
        unmockkStatic("io.ktor.client.plugins.websocket.BuildersKt")
        activeJob.cancel()
    }

    private fun buildNostrSocketClient(httpClient: HttpClient = mockHttpClient) =
        NostrSocketClientImpl(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            httpClient = httpClient,
            wssUrl = "wss://relay.primal.net",
        )

    @Test
    fun sendREQ_sendsTextFrameToWebSocket() =
        runTest {
            val frameSlot = slot<Frame>()
            coEvery { mockWebSocketSession.send(capture(frameSlot)) } just Runs
            val client = buildNostrSocketClient()
            client.ensureSocketConnectionOrThrow()

            val subscriptionId = Uuid.random().toPrimalSubscriptionId()
            client.sendREQ(
                subscriptionId = subscriptionId,
                data = buildJsonObject {},
            )

            coVerify { mockWebSocketSession.send(any<Frame>()) }
            val sentFrame = frameSlot.captured
            sentFrame shouldNotBe null
        }

    @Test
    fun sendEVENT_sendsTextFrameToWebSocket() =
        runTest {
            coEvery { mockWebSocketSession.send(any<Frame>()) } just Runs
            val client = buildNostrSocketClient()
            client.ensureSocketConnectionOrThrow()

            client.sendEVENT(signedEvent = buildJsonObject {})

            coVerify { mockWebSocketSession.send(any<Frame>()) }
        }

    @Test
    fun sendAUTH_sendsTextFrameToWebSocket() =
        runTest {
            coEvery { mockWebSocketSession.send(any<Frame>()) } just Runs
            val client = buildNostrSocketClient()
            client.ensureSocketConnectionOrThrow()

            client.sendAUTH(signedEvent = buildJsonObject {})

            coVerify { mockWebSocketSession.send(any<Frame>()) }
        }

    @Test
    fun sendCOUNT_returnsSubscriptionId() =
        runTest {
            coEvery { mockWebSocketSession.send(any<Frame>()) } just Runs
            val client = buildNostrSocketClient()
            client.ensureSocketConnectionOrThrow()

            val subscriptionId = client.sendCOUNT(data = buildJsonObject {})

            subscriptionId shouldNotBe null
            coVerify { mockWebSocketSession.send(any<Frame>()) }
        }

    @Test
    fun sendCLOSE_sendsTextFrameToWebSocket() =
        runTest {
            coEvery { mockWebSocketSession.send(any<Frame>()) } just Runs
            val client = buildNostrSocketClient()
            client.ensureSocketConnectionOrThrow()

            val subscriptionId = Uuid.random().toPrimalSubscriptionId()
            client.sendCLOSE(subscriptionId = subscriptionId)

            coVerify { mockWebSocketSession.send(any<Frame>()) }
        }

    @Test
    fun ensureSocketConnection_connectsViaHttpClient() =
        runTest {
            val client = buildNostrSocketClient()

            client.ensureSocketConnectionOrThrow()

            coVerify { mockHttpClient.webSocketSession(urlString = any<String>()) }
        }

    @Test
    fun ensureSocketConnection_whenConnectionFails_throwsNetworkException() =
        runTest {
            val failingHttpClient = mockk<HttpClient>(relaxed = true)
            coEvery {
                failingHttpClient.webSocketSession(urlString = any<String>())
            } throws RuntimeException("Connection failed")
            val client = buildNostrSocketClient(httpClient = failingHttpClient)

            shouldThrow<NetworkException> {
                client.ensureSocketConnectionOrThrow()
            }
        }

    @Test
    fun socketUrl_cleansHttpsToWss() {
        val client = NostrSocketClientImpl(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            httpClient = mockHttpClient,
            wssUrl = "https://relay.primal.net/v1",
        )

        client.socketUrl shouldBe "wss://relay.primal.net/v1"
    }

    @Test
    fun socketUrl_removesTrailingSlash() {
        val client = NostrSocketClientImpl(
            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
            httpClient = mockHttpClient,
            wssUrl = "wss://relay.primal.net/v1/",
        )

        client.socketUrl shouldBe "wss://relay.primal.net/v1"
    }
}
