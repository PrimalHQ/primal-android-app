package net.primal.networking.sockets

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlin.time.TimeSource
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import net.primal.core.networking.primal.BasePrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.NostrSocketClientImpl
import net.primal.core.networking.sockets.subscription
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

    private fun buildNostrSocketClient(
        httpClient: HttpClient = mockHttpClient,
        timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    ) = NostrSocketClientImpl(
        dispatcherProvider = coroutinesTestRule.dispatcherProvider,
        httpClient = httpClient,
        wssUrl = "wss://relay.primal.net",
        timeSource = timeSource,
    )

    private fun newWebSocketSession(
        incomingFrames: Channel<Frame>,
        sentFrames: MutableList<Frame>,
    ): DefaultClientWebSocketSession =
        mockk(relaxed = true) {
            every { coroutineContext } returns (Job() as CoroutineContext)
            every { incoming } returns incomingFrames
            coEvery { send(capture(sentFrames)) } just Runs
        }

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

    @Test
    fun ensureConnection_whenSilentAfterSend_rebuildsSocket() =
        runTest {
            val timeSource = TestTimeSource()
            coEvery { mockWebSocketSession.send(any<Frame>()) } just Runs
            val client = buildNostrSocketClient(timeSource = timeSource)

            client.ensureSocketConnectionOrThrow()
            client.sendREQ(
                subscriptionId = Uuid.random().toPrimalSubscriptionId(),
                data = buildJsonObject {},
            )

            timeSource += 11.seconds
            client.ensureSocketConnectionOrThrow()

            coVerify(exactly = 2) { mockHttpClient.webSocketSession(urlString = any<String>()) }
            activeJob.isCancelled shouldBe true
        }

    @Test
    fun ensureConnection_bumpsConnectionGenerationOnEachConnect() =
        runTest {
            val timeSource = TestTimeSource()
            coEvery { mockWebSocketSession.send(any<Frame>()) } just Runs
            val client = buildNostrSocketClient(timeSource = timeSource)

            client.connectionGeneration.value shouldBe 0L

            client.ensureSocketConnectionOrThrow()
            client.connectionGeneration.value shouldBe 1L

            // Go silent past the watchdog window so the next ensure rebuilds the socket.
            client.sendREQ(
                subscriptionId = Uuid.random().toPrimalSubscriptionId(),
                data = buildJsonObject {},
            )
            timeSource += 11.seconds
            client.ensureSocketConnectionOrThrow()

            client.connectionGeneration.value shouldBe 2L
        }

    @Test
    fun ensureConnection_whenIdleWithoutSending_doesNotRebuild() =
        runTest {
            val timeSource = TestTimeSource()
            val client = buildNostrSocketClient(timeSource = timeSource)

            client.ensureSocketConnectionOrThrow()
            timeSource += 30.seconds
            client.ensureSocketConnectionOrThrow()

            coVerify(exactly = 1) { mockHttpClient.webSocketSession(urlString = any<String>()) }
        }

    @Test
    fun ensureConnection_whenFrameReceivedAfterSend_doesNotRebuild() =
        runTest {
            val timeSource = TestTimeSource()
            val incomingChannel = Channel<Frame>(capacity = Channel.UNLIMITED)
            every { mockWebSocketSession.incoming } returns incomingChannel
            coEvery { mockWebSocketSession.send(any<Frame>()) } just Runs
            val client = buildNostrSocketClient(timeSource = timeSource)

            client.ensureSocketConnectionOrThrow()
            client.sendREQ(
                subscriptionId = Uuid.random().toPrimalSubscriptionId(),
                data = buildJsonObject {},
            )

            timeSource += 1.seconds
            // Frame content is irrelevant — any inbound frame proves the peer is answering.
            incomingChannel.send(Frame.Text(text = "noop"))
            advanceUntilIdle()

            timeSource += 11.seconds
            client.ensureSocketConnectionOrThrow()

            coVerify(exactly = 1) { mockHttpClient.webSocketSession(urlString = any<String>()) }

            incomingChannel.close()
            advanceUntilIdle()
        }

    @Test
    fun query_afterUnansweredRequest_rebuildsSocketAndServesNextQuery() =
        runTest {
            val timeSource = TestTimeSource()
            val incomingChannel = Channel<Frame>(capacity = Channel.UNLIMITED)
            val sentFrames = mutableListOf<Frame>()
            every { mockWebSocketSession.incoming } returns incomingChannel
            coEvery { mockWebSocketSession.send(capture(sentFrames)) } just Runs

            val socketClient = buildNostrSocketClient(timeSource = timeSource)
            val apiClient = BasePrimalApiClient(socketClient = socketClient)

            // A request is sent but the backend never returns a terminating frame, so it
            // times out — this is the stuck request that used to wedge the shared socket.
            shouldThrow<Throwable> {
                apiClient.query(message = PrimalCacheFilter(primalVerb = "stuck_verb"))
            }

            // The socket has now been silent (sent, nothing received) past the watchdog window.
            timeSource += 11.seconds

            // A new query must NOT reuse the dead socket: the watchdog rebuilds it and the
            // query is served on the fresh session instead of starving like everything else did.
            val deferredResult = async {
                apiClient.query(message = PrimalCacheFilter(primalVerb = "next_verb"))
            }
            runCurrent()

            val reqText = (sentFrames.last() as Frame.Text).readText()
            val nextSubId = Regex("\"REQ\",\"([^\"]+)\"").find(reqText)?.groupValues?.get(1)
            incomingChannel.send(Frame.Text(text = "[\"EOSE\",\"$nextSubId\"]"))
            advanceTimeBy(100.milliseconds)
            runCurrent()

            val result = deferredResult.await()
            result.terminationMessage shouldNotBe null

            // The dead socket was replaced, not reused: two sessions were acquired.
            coVerify(exactly = 2) { mockHttpClient.webSocketSession(urlString = any<String>()) }

            incomingChannel.close()
            advanceUntilIdle()
        }

    @Test
    fun subscription_replaysReqAfterSocketRebuild() =
        runTest {
            val timeSource = TestTimeSource()
            val incomingChannel = Channel<Frame>(capacity = Channel.UNLIMITED)
            val sentFrames = mutableListOf<Frame>()
            coEvery {
                mockHttpClient.webSocketSession(urlString = any<String>())
            } answers { newWebSocketSession(incomingChannel, sentFrames) }
            val client = buildNostrSocketClient(timeSource = timeSource)

            val subscriptionId = Uuid.random().toPrimalSubscriptionId()
            val job = launch {
                client.subscription(subscriptionId = subscriptionId, data = buildJsonObject {}).collect { }
            }
            runCurrent()

            sentFrames.reqCountFor(subscriptionId) shouldBe 1

            timeSource += 11.seconds
            client.ensureSocketConnectionOrThrow()
            runCurrent()

            coVerify(exactly = 2) { mockHttpClient.webSocketSession(urlString = any<String>()) }
            sentFrames.reqCountFor(subscriptionId) shouldBe 2

            job.cancel()
            incomingChannel.close()
            advanceUntilIdle()
        }

    @Test
    fun subscription_afterCompletion_sendsCloseAndStopsReplaying() =
        runTest {
            val timeSource = TestTimeSource()
            val incomingChannel = Channel<Frame>(capacity = Channel.UNLIMITED)
            val sentFrames = mutableListOf<Frame>()
            every { mockWebSocketSession.incoming } returns incomingChannel
            coEvery { mockWebSocketSession.send(capture(sentFrames)) } just Runs
            val client = buildNostrSocketClient(timeSource = timeSource)

            val subscriptionId = Uuid.random().toPrimalSubscriptionId()
            val job = launch {
                client.subscription(subscriptionId = subscriptionId, data = buildJsonObject {}).collect { }
            }
            runCurrent()
            sentFrames.reqCountFor(subscriptionId) shouldBe 1

            // Completing the collection must send CLOSE.
            job.cancel()
            advanceUntilIdle()
            sentFrames.closeCountFor(subscriptionId) shouldBe 1

            // A rebuild after completion must NOT replay the closed subscription.
            timeSource += 11.seconds
            client.ensureSocketConnectionOrThrow()
            runCurrent()
            sentFrames.reqCountFor(subscriptionId) shouldBe 1

            incomingChannel.close()
            advanceUntilIdle()
        }

    @Test
    fun subscribe_isReplayedAfterSocketRebuild() =
        runTest {
            val timeSource = TestTimeSource()
            val incomingChannel = Channel<Frame>(capacity = Channel.UNLIMITED)
            val sentFrames = mutableListOf<Frame>()

            coEvery {
                mockHttpClient.webSocketSession(urlString = any<String>())
            } answers { newWebSocketSession(incomingChannel, sentFrames) }

            val socketClient = buildNostrSocketClient(timeSource = timeSource)
            val apiClient = BasePrimalApiClient(socketClient = socketClient)

            val subscriptionId = Uuid.random().toPrimalSubscriptionId()
            val job = launch {
                apiClient.subscribe(
                    subscriptionId = subscriptionId,
                    message = PrimalCacheFilter(primalVerb = "sub_verb"),
                ).collect { }
            }
            runCurrent()

            timeSource += 11.seconds
            socketClient.ensureSocketConnectionOrThrow()
            runCurrent()

            coVerify(exactly = 2) { mockHttpClient.webSocketSession(urlString = any<String>()) }
            sentFrames.reqCountFor(subscriptionId) shouldBe 2

            job.cancel()
            incomingChannel.close()
            advanceUntilIdle()
        }

    @Test
    fun ensureConnection_afterRebuild_doesNotReplayPlainReq() =
        runTest {
            val timeSource = TestTimeSource()
            val sentFrames = mutableListOf<Frame>()
            coEvery { mockWebSocketSession.send(capture(sentFrames)) } just Runs
            val client = buildNostrSocketClient(timeSource = timeSource)

            client.ensureSocketConnectionOrThrow()
            val subscriptionId = Uuid.random().toPrimalSubscriptionId()
            // A plain query-style REQ (not via subscription()) must never be replayed.
            client.sendREQ(subscriptionId = subscriptionId, data = buildJsonObject {})

            timeSource += 11.seconds
            client.ensureSocketConnectionOrThrow()

            sentFrames.reqCountFor(subscriptionId) shouldBe 1
        }

    private fun List<Frame>.reqCountFor(subscriptionId: String): Int =
        filterIsInstance<Frame.Text>().count { it.readText().contains("\"REQ\",\"$subscriptionId\"") }

    private fun List<Frame>.closeCountFor(subscriptionId: String): Int =
        filterIsInstance<Frame.Text>().count { it.readText().contains("\"CLOSE\",\"$subscriptionId\"") }
}
