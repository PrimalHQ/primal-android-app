package net.primal.android.networking.sockets

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.networking.FakeWebSocket
import net.primal.android.networking.FakeWebSocketOkHttpClient
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NostrSocketClientTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private fun buildFakeNostrSocketClient(webSocket: FakeWebSocket) = NostrSocketClient(
        dispatcherProvider = coroutinesTestRule.dispatcherProvider,
        okHttpClient = FakeWebSocketOkHttpClient(webSocket = webSocket),
        wssRequest = mockk(relaxed = true),
    )

    private fun buildSuccessfulNostrSocketClient() =
        buildFakeNostrSocketClient(webSocket = FakeWebSocket(sendResponse = true))

    private fun buildFailingNostrSocketClient() =
        buildFakeNostrSocketClient(webSocket = FakeWebSocket(sendResponse = false))

    @Test
    fun sendREQWithSubscriptionId_returnsTrueIfMessageWasSent() = runTest {
        val client = buildSuccessfulNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendREQ(
            subscriptionId = UUID.randomUUID(),
            data = buildJsonObject {},
        )
        actual shouldBe true
    }

    @Test
    fun sendREQWithSubscriptionId_returnsFalseIfMessageWasNotSent() = runTest {
        val client = buildFailingNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendREQ(
            subscriptionId = UUID.randomUUID(),
            data = buildJsonObject {},
        )
        actual shouldBe false
    }

    @Test
    fun sendREQ_returnsSubscriptionIdIfMessageWasSent() = runTest {
        val client = buildSuccessfulNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendREQ(data = buildJsonObject {})
        actual.shouldNotBeNull()
    }

    @Test
    fun sendREQ_returnsNullIfMessageWasNotSent() = runTest {
        val client = buildFailingNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendREQ(data = buildJsonObject {})
        actual.shouldBeNull()
    }

    @Test
    fun sendEVENT_returnsTrueIfMessageWasSent() = runTest {
        val client = buildSuccessfulNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendEVENT(signedEvent = buildJsonObject {})
        actual shouldBe true
    }

    @Test
    fun sendEVENT_returnsFalseIfMessageWasNotSent() = runTest {
        val client = buildFailingNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendEVENT(signedEvent = buildJsonObject {})
        actual shouldBe false
    }

    @Test
    fun sendAUTH_returnsTrueIfMessageWasSent() = runTest {
        val client = buildSuccessfulNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendAUTH(signedEvent = buildJsonObject {})
        actual shouldBe true
    }

    @Test
    fun sendAUTH_returnsFalseIfMessageWasNotSent() = runTest {
        val client = buildFailingNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendAUTH(signedEvent = buildJsonObject {})
        actual shouldBe false
    }

    @Test
    fun sendCOUNT_returnsSubscriptionIdIfMessageWasSent() = runTest {
        val client = buildSuccessfulNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendCOUNT(data = buildJsonObject {})
        actual.shouldNotBeNull()
    }

    @Test
    fun sendCOUNT_returnsNullIfMessageWasNotSent() = runTest {
        val client = buildFailingNostrSocketClient()
        client.ensureSocketConnection()
        val actual = client.sendCOUNT(data = buildJsonObject {})
        actual.shouldBeNull()
    }

    @Test
    fun sendCLOSE_callsSendOnWebSocket() = runTest {
        val mockWebSocket = mockk<FakeWebSocket>(relaxed = true)
        val fakeNostrSocketClient = buildFakeNostrSocketClient(webSocket = mockWebSocket)
        fakeNostrSocketClient.ensureSocketConnection()
        val subscriptionId = UUID.randomUUID()
        fakeNostrSocketClient.sendCLOSE(subscriptionId = subscriptionId)

        verify {
            mockWebSocket.send(
                withArg<String> {
                    it shouldBe subscriptionId.buildNostrCLOSEMessage()
                },
            )
        }
    }
}
