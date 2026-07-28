package net.primal.networking.sockets

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.filterByEventId
import net.primal.core.networking.sockets.filterBySubscriptionId

class NostrIncomingMessageExtTest {

    @org.junit.Test
    fun `filterBySubscriptionId drops NOTICE addressed to another subscription`() =
        runTest {
            val eose = NostrIncomingMessage.EoseMessage(subscriptionId = "my-sub")
            val messages = flowOf(
                NostrIncomingMessage.NoticeMessage(subscriptionId = "other-sub", message = "error"),
                eose,
            )

            val actual = messages.filterBySubscriptionId(id = "my-sub").toList()

            actual shouldBe listOf(eose)
        }

    @org.junit.Test
    fun `filterBySubscriptionId keeps NOTICE addressed to this subscription`() =
        runTest {
            val notice = NostrIncomingMessage.NoticeMessage(subscriptionId = "my-sub", message = "error")

            val actual = flowOf(notice).filterBySubscriptionId(id = "my-sub").toList()

            actual shouldBe listOf(notice)
        }

    @org.junit.Test
    fun `filterBySubscriptionId keeps NOTICE with no subscription id`() =
        runTest {
            val notice = NostrIncomingMessage.NoticeMessage(subscriptionId = null, message = "server restarting")

            val actual = flowOf(notice).filterBySubscriptionId(id = "my-sub").toList()

            actual shouldBe listOf(notice)
        }

    @org.junit.Test
    fun `filterByEventId drops NOTICE addressed to a subscription`() =
        runTest {
            val ok = NostrIncomingMessage.OkMessage(eventId = "my-event", success = true)
            val messages = flowOf(
                NostrIncomingMessage.NoticeMessage(subscriptionId = "other-sub", message = "error"),
                ok,
            )

            val actual = messages.filterByEventId(id = "my-event").toList()

            actual shouldBe listOf(ok)
        }

    @org.junit.Test
    fun `filterByEventId keeps NOTICE with no subscription id`() =
        runTest {
            val notice = NostrIncomingMessage.NoticeMessage(subscriptionId = null, message = "invalid event")

            val actual = flowOf(notice).filterByEventId(id = "my-event").toList()

            actual shouldBe listOf(notice)
        }
}
