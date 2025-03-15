package net.primal.core.networking.sockets

import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

sealed class NostrIncomingMessage {

    data class EventMessage(
        val subscriptionId: String,
        val nostrEvent: NostrEvent? = null,
        val primalEvent: PrimalEvent? = null,
    ) : NostrIncomingMessage()

    data class EoseMessage(
        val subscriptionId: String,
    ) : NostrIncomingMessage()

    data class OkMessage(
        val eventId: String,
        val success: Boolean,
        val message: String? = null,
    ) : NostrIncomingMessage()

    data class NoticeMessage(
        val subscriptionId: String? = null,
        val message: String? = null,
    ) : NostrIncomingMessage()

    data class AuthMessage(
        val challenge: String,
    ) : NostrIncomingMessage()

    data class CountMessage(
        val subscriptionId: String,
        val count: Int,
    ) : NostrIncomingMessage()

    data class EventsMessage(
        val subscriptionId: String,
        val nostrEvents: List<NostrEvent> = emptyList(),
        val primalEvents: List<PrimalEvent> = emptyList(),
    ) : NostrIncomingMessage()
}
