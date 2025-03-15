package net.primal.core.networking.sockets


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

internal fun Flow<NostrIncomingMessage>.filterBySubscriptionId(id: String) =
    filter {
        (it is NostrIncomingMessage.EventMessage && it.subscriptionId == id) ||
            (it is NostrIncomingMessage.EoseMessage && it.subscriptionId == id) ||
            (it is NostrIncomingMessage.CountMessage && it.subscriptionId == id) ||
            (it is NostrIncomingMessage.EventsMessage && it.subscriptionId == id) ||
            (it is NostrIncomingMessage.NoticeMessage)
    }


internal fun Flow<NostrIncomingMessage>.filterByEventId(id: String) =
    filter {
        (it is NostrIncomingMessage.OkMessage && it.eventId == id) ||
            (it is NostrIncomingMessage.NoticeMessage)
    }
