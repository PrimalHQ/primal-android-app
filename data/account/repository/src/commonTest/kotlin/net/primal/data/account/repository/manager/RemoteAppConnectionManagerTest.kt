package net.primal.data.account.repository.manager

import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.primal.domain.account.model.RemoteAppConnectionStatus

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteAppConnectionManagerTest {

    @Test
    fun lastConnectedAt_isUpdatedOnlyOnInitialConnection() =
        runTest {
            val manager = RemoteAppConnectionManager()
            val sessionId = "session1"
            val relay1 = "wss://relay1.com"
            val relay2 = "wss://relay2.com"

            manager.onRelayConnected(sessionId, relay1)
            val state1 = manager.observeSessionConnectionState(sessionId).first()
            state1.shouldNotBeNull()
            state1.status shouldBe RemoteAppConnectionStatus.Connected
            val firstConnectedAt = state1.lastConnectedAt
            firstConnectedAt.shouldNotBeNull()

            manager.onRelayConnected(sessionId, relay2)
            val state2 = manager.observeSessionConnectionState(sessionId).first()
            state2.shouldNotBeNull()
            state2.status shouldBe RemoteAppConnectionStatus.Connected

            state2.lastConnectedAt shouldBe firstConnectedAt
        }

    @Test
    fun lastConnectedAt_isResetIfDisconnectedAndThenConnected() =
        runTest {
            val manager = RemoteAppConnectionManager()
            val sessionId = "session1"
            val relay1 = "wss://relay1.com"

            manager.onRelayConnected(sessionId, relay1)
            val state1 = manager.observeSessionConnectionState(sessionId).first()
            state1.shouldNotBeNull()
            val firstConnectedAt = state1.lastConnectedAt
            firstConnectedAt.shouldNotBeNull()

            manager.onRelayDisconnected(sessionId, relay1, null)
            val stateDisconnected = manager.observeSessionConnectionState(sessionId).first()
            stateDisconnected.shouldNotBeNull()
            stateDisconnected.status shouldBe RemoteAppConnectionStatus.Disconnected
            stateDisconnected.lastConnectedAt shouldBe firstConnectedAt

            manager.onRelayConnected(sessionId, relay1)
            val state2 = manager.observeSessionConnectionState(sessionId).first()
            state2.shouldNotBeNull()
            state2.status shouldBe RemoteAppConnectionStatus.Connected

            val secondConnectedAt = state2.lastConnectedAt
            secondConnectedAt.shouldNotBeNull()
            secondConnectedAt shouldBeGreaterThanOrEqualTo firstConnectedAt
        }
}
