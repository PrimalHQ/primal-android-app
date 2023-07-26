package net.primal.android.networking.sockets

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import okhttp3.WebSocket
import org.junit.Test
import java.util.UUID

class NostrSocketClientTest {

    private fun fakeNostrSocketClient(webSocket: WebSocket) = NostrSocketClient(
        okHttpClient = mockk(relaxed = true) {
            every { newWebSocket(any(), any()) } returns webSocket
        },
        wssRequest = mockk(relaxed = true)
    )

    private val successfulNostrSocketClient = fakeNostrSocketClient(
        webSocket = mockk(relaxed = true) {
            every { send(any<String>()) } returns true
        }
    )

    private val failingNostrSocketClient = fakeNostrSocketClient(
        webSocket = mockk(relaxed = true) {
            every { send(any<String>()) } returns false
        }
    )

    @Test
    fun `sendREQ returns subscription UUID if message was sent`() = runTest {
        successfulNostrSocketClient.ensureSocketConnection()
        val actual = successfulNostrSocketClient.sendREQ(data = buildJsonObject {})
        actual.shouldNotBeNull()
    }

    @Test
    fun `sendREQ returns null if message was not sent`() = runTest {
        failingNostrSocketClient.ensureSocketConnection()
        val actual = failingNostrSocketClient.sendREQ(data = buildJsonObject {})
        actual.shouldBeNull()
    }

    @Test
    fun `sendEVENT returns true if message was sent`() = runTest {
        successfulNostrSocketClient.ensureSocketConnection()
        val actual = successfulNostrSocketClient.sendEVENT(signedEvent = buildJsonObject {})
        actual shouldBe true
    }

    @Test
    fun `sendEVENT returns false if message was not sent`() = runTest {
        failingNostrSocketClient.ensureSocketConnection()
        val actual = failingNostrSocketClient.sendEVENT(signedEvent = buildJsonObject {})
        actual shouldBe false
    }

    @Test
    fun `sendAUTH returns true if message was sent`() = runTest {
        successfulNostrSocketClient.ensureSocketConnection()
        val actual = successfulNostrSocketClient.sendAUTH(signedEvent = buildJsonObject {})
        actual shouldBe true
    }

    @Test
    fun `sendAUTH returns false if message was not sent`() = runTest {
        failingNostrSocketClient.ensureSocketConnection()
        val actual = failingNostrSocketClient.sendAUTH(signedEvent = buildJsonObject {})
        actual shouldBe false
    }

    @Test
    fun `sendCOUNT returns subscription UUID if message was sent`() = runTest {
        successfulNostrSocketClient.ensureSocketConnection()
        val actual = successfulNostrSocketClient.sendCOUNT(data = buildJsonObject {})
        actual.shouldNotBeNull()
    }

    @Test
    fun `sendCOUNT returns null if message was not sent`() = runTest {
        failingNostrSocketClient.ensureSocketConnection()
        val actual = failingNostrSocketClient.sendCOUNT(data = buildJsonObject {})
        actual.shouldBeNull()
    }

    @Test
    fun `sendCLOSE calls send on webSocket`() = runTest {
        val webSocketSpy: WebSocket = mockk(relaxed = true)
        val fakeNostrSocketClient = fakeNostrSocketClient(webSocket = webSocketSpy)
        fakeNostrSocketClient.ensureSocketConnection()
        val subscriptionId = UUID.randomUUID()
        fakeNostrSocketClient.sendCLOSE(subscriptionId = subscriptionId)

        verify {
            webSocketSpy.send(
                withArg<String> {
                    it shouldBe subscriptionId.buildNostrCLOSEMessage()
                }
            )
        }
    }
}
