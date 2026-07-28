package net.primal.core.networking.sockets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

fun Flow<NostrIncomingMessage>.filterBySubscriptionId(id: String) =
    filter {
        (it is NostrIncomingMessage.EventMessage && it.subscriptionId == id) ||
            (it is NostrIncomingMessage.EoseMessage && it.subscriptionId == id) ||
            (it is NostrIncomingMessage.CountMessage && it.subscriptionId == id) ||
            (it is NostrIncomingMessage.EventsMessage && it.subscriptionId == id) ||
            // An addressed NOTICE belongs to one subscription; only unaddressed ones are socket-wide.
            (it is NostrIncomingMessage.NoticeMessage && (it.subscriptionId == null || it.subscriptionId == id))
    }

fun Flow<NostrIncomingMessage>.filterByEventId(id: String) =
    filter {
        (it is NostrIncomingMessage.OkMessage && it.eventId == id) ||
            // [id] is an event id, so a subscription-addressed NOTICE can never concern this publish.
            (it is NostrIncomingMessage.NoticeMessage && it.subscriptionId == null)
    }
