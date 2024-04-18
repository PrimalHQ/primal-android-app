package net.primal.android.networking.relays

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.test.runTest
import net.primal.android.core.coroutines.CoroutinesTestRule
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.NostrIncomingMessage
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.nostr.model.NostrEvent
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RelayPoolTest {

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()

    private fun buildNostrEvent(eventId: String): NostrEvent {
        return NostrEvent(
            id = eventId,
            pubKey = "01234",
            kind = 0,
            content = "",
            createdAt = System.currentTimeMillis() / 1_000,
            sig = "sig",
        )
    }

    private fun buildSocketClientReturningOkMessageSuccessFalse(
        scope: CoroutineScope,
        eventId: String,
    ): NostrSocketClient {
        return mockk<NostrSocketClient>(relaxed = true) {
            every { incomingMessages } returns flowOf(
                NostrIncomingMessage.OkMessage(eventId = eventId, success = false),
            ).shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
            )
        }
    }

    private fun buildSocketClientReturningOkMessageSuccessTrue(
        scope: CoroutineScope,
        eventId: String,
    ): NostrSocketClient {
        return mockk<NostrSocketClient>(relaxed = true) {
            every { incomingMessages } returns flowOf(
                NostrIncomingMessage.OkMessage(eventId = eventId, success = true),
            ).shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
            )
        }
    }

    private fun buildSocketClientReturningNoticeMessage(
        scope: CoroutineScope,
    ): NostrSocketClient {
        return mockk<NostrSocketClient>(relaxed = true) {
            every { incomingMessages } returns flowOf(
                NostrIncomingMessage.NoticeMessage(message = "This is your notice!"),
            ).shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
            )
        }
    }

    private fun buildSocketClientNotReturningAnything(
        scope: CoroutineScope,
    ): NostrSocketClient {
        return mockk<NostrSocketClient>(relaxed = true) {
            every { incomingMessages } returns emptyFlow<NostrIncomingMessage>().shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
            )
        }
    }

    @Test(expected = NostrPublishException::class)
    fun publishEvent_throwsIfAllPublishesFail() = runTest {
        val relayPool = RelayPool(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            okHttpClient = mockk(relaxed = true),
        )
        val eventId = "randomThrowId"
        val nostrEvent = buildNostrEvent(eventId = eventId)

        relayPool.socketClients = listOf(
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
        )

        relayPool.publishEvent(nostrEvent)
    }

    @Test
    fun publishEvent_doesNotThrowIfAtLeastOnePublishIsSuccessful() = runTest {
        val relayPool = RelayPool(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            okHttpClient = mockk(relaxed = true),
        )
        val eventId = "randomSuccessId"
        val nostrEvent = buildNostrEvent(eventId = eventId)

        relayPool.socketClients = listOf(
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessTrue(scope = this, eventId = eventId),
        )

        relayPool.publishEvent(nostrEvent)
    }

    @Test
    fun publishEvent_doesNotThrowIfWeHaveSuccessPublishAndSomeRelaysReturnNoticeMessage() = runTest {
        val relayPool = RelayPool(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            okHttpClient = mockk(relaxed = true),
        )
        val eventId = "randomSuccessId2"
        val nostrEvent = buildNostrEvent(eventId = eventId)

        relayPool.socketClients = listOf(
            buildSocketClientReturningNoticeMessage(scope = this),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessTrue(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
        )

        relayPool.publishEvent(nostrEvent)
    }

    @Test
    fun publishEvent_throwsImmediatelyIfAllPublishesFail() = runTest {
        val relayPool = RelayPool(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            okHttpClient = mockk(relaxed = true),
        )
        val eventId = "randomThrowId"
        val nostrEvent = buildNostrEvent(eventId = eventId)

        relayPool.socketClients = listOf(
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
        )

        val startTime = testScheduler.currentTime
        try { relayPool.publishEvent(nostrEvent) } catch (_: NostrPublishException) {}
        val endTime = testScheduler.currentTime

        endTime - startTime shouldBe 0
    }

    @Test
    fun publishEvent_timeoutsAfterSomeTimeIfWeAreStillWaitingAndNoSuccessfulMessages() = runTest {
        val relayPool = RelayPool(
            dispatchers = coroutinesTestRule.dispatcherProvider,
            okHttpClient = mockk(relaxed = true),
        )
        val eventId = "randomTimeoutId"
        val nostrEvent = buildNostrEvent(eventId = eventId)

        relayPool.socketClients = listOf(
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientReturningOkMessageSuccessFalse(scope = this, eventId = eventId),
            buildSocketClientNotReturningAnything(scope = this),
        )

        val startTime = testScheduler.currentTime
        try { relayPool.publishEvent(nostrEvent) } catch (_: NostrPublishException) {}
        val endTime = testScheduler.currentTime

        endTime - startTime shouldBe RelayPool.PUBLISH_TIMEOUT
    }
}
