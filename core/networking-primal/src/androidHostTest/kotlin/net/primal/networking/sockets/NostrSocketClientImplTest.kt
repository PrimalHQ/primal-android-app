package net.primal.networking.sockets

import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.primal.core.coroutines.CoroutinesTestRule

@OptIn(ExperimentalCoroutinesApi::class)
class NostrSocketClientImplTest {

    @get:org.junit.Rule
    val coroutinesTestRule = CoroutinesTestRule()

    // TODO Fix NostrSocketClientImplTest

//    private fun buildFakeNostrSocketClient(webSocket: FakeWebSocket) =
//        NostrSocketClientImpl(
//            dispatcherProvider = coroutinesTestRule.dispatcherProvider,
//            okHttpClient = FakeWebSocketOkHttpClient(webSocket = webSocket),
//            wssRequest = mockk(relaxed = true),
//        )
//
//    private fun buildSuccessfulNostrSocketClient() =
//        buildFakeNostrSocketClient(webSocket = FakeWebSocket(sendResponse = true))
//
//    private fun buildFailingNostrSocketClient() =
//        buildFakeNostrSocketClient(webSocket = FakeWebSocket(sendResponse = false))

//    @Test
//    fun sendREQWithSubscriptionId_returnsTrueIfMessageWasSent() =
//        runTest {
//            val client = buildSuccessfulNostrSocketClient()
//            client.ensureSocketConnection()
//            val actual = client.sendREQ(
//                subscriptionId = Uuid.random().toPrimalSubscriptionId(),
//                data = buildJsonObject {},
//            )
//            actual shouldBe true
//        }
//
//    @Test
//    fun sendREQWithSubscriptionId_returnsFalseIfMessageWasNotSent() =
//        runTest {
//            val client = buildFailingNostrSocketClient()
//            client.ensureSocketConnection()
//            val actual = client.sendREQ(
//                subscriptionId = Uuid.random().toPrimalSubscriptionId(),
//                data = buildJsonObject {},
//            )
//            actual shouldBe false
//        }
//
//    @Test
//    fun sendEVENT_returnsTrueIfMessageWasSent() =
//        runTest {
//            val client = buildSuccessfulNostrSocketClient()
//            client.ensureSocketConnection()
//            val actual = client.sendEVENT(signedEvent = buildJsonObject {})
//            actual shouldBe true
//        }
//
//    @Test
//    fun sendEVENT_returnsFalseIfMessageWasNotSent() =
//        runTest {
//            val client = buildFailingNostrSocketClient()
//            client.ensureSocketConnection()
//            val actual = client.sendEVENT(signedEvent = buildJsonObject {})
//            actual shouldBe false
//        }
//
//    @Test
//    fun sendAUTH_returnsTrueIfMessageWasSent() =
//        runTest {
//            val client = buildSuccessfulNostrSocketClient()
//            client.ensureSocketConnection()
//            val actual = client.sendAUTH(signedEvent = buildJsonObject {})
//            actual shouldBe true
//        }
//
//    @Test
//    fun sendAUTH_returnsFalseIfMessageWasNotSent() =
//        runTest {
//            val client = buildFailingNostrSocketClient()
//            client.ensureSocketConnection()
//            val actual = client.sendAUTH(signedEvent = buildJsonObject {})
//            actual shouldBe false
//        }
//
//    @Test
//    fun sendCOUNT_returnsSubscriptionIdIfMessageWasSent() =
//        runTest {
//            val client = buildSuccessfulNostrSocketClient()
//            client.ensureSocketConnection()
//            val actual = client.sendCOUNT(data = buildJsonObject {})
//            actual.shouldNotBeNull()
//        }
//
//    @Test
//    fun sendCOUNT_returnsNullIfMessageWasNotSent() =
//        runTest {
//            val client = buildFailingNostrSocketClient()
//            client.ensureSocketConnection()
//            val actual = client.sendCOUNT(data = buildJsonObject {})
//            actual.shouldBeNull()
//        }

//    @Test
//    fun sendCLOSE_callsSendOnWebSocket() =
//        runTest {
//            val mockWebSocket = mockk<FakeWebSocket>(relaxed = true)
//            val fakeNostrSocketClient = buildFakeNostrSocketClient(webSocket = mockWebSocket)
//            fakeNostrSocketClient.ensureSocketConnection()
//            val subscriptionId = Uuid.random().toPrimalSubscriptionId(),
//            fakeNostrSocketClient.sendCLOSE(subscriptionId = subscriptionId)
//
//            verify {
//                mockWebSocket.send(
//                    withArg<String> {
//                        it shouldBe subscriptionId.buildNostrCLOSEMessage()
//                    },
//                )
//            }
//        }
}
