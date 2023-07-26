package net.primal.android.networking.sockets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.util.UUID

fun Flow<NostrIncomingMessage>.filterBySubscriptionId(id: UUID) = filter {
    (it is NostrIncomingMessage.EventMessage && it.subscriptionId == id)
            || (it is NostrIncomingMessage.EoseMessage && it.subscriptionId == id)
            || (it is NostrIncomingMessage.CountMessage && it.subscriptionId == id)
            || (it is NostrIncomingMessage.NoticeMessage)
}

fun Flow<NostrIncomingMessage>.filterByEventId(id: String) = filter {
    (it is NostrIncomingMessage.OkMessage && it.eventId == id)
            || (it is NostrIncomingMessage.NoticeMessage)
}
