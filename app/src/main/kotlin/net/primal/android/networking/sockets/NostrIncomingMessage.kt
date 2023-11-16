package net.primal.android.networking.sockets

import java.util.UUID
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

sealed class NostrIncomingMessage {

    data class EventMessage(
        val subscriptionId: UUID,
        val nostrEvent: NostrEvent? = null,
        val primalEvent: PrimalEvent? = null,
    ) : NostrIncomingMessage()

    data class EoseMessage(
        val subscriptionId: UUID,
    ) : NostrIncomingMessage()

    data class OkMessage(
        val eventId: String,
        val success: Boolean,
        val message: String? = null,
    ) : NostrIncomingMessage()

    data class NoticeMessage(
        val message: String? = null,
    ) : NostrIncomingMessage()

    data class AuthMessage(
        val challenge: String,
    ) : NostrIncomingMessage()

    data class CountMessage(
        val subscriptionId: UUID,
        val count: Int,
    ) : NostrIncomingMessage()
}
