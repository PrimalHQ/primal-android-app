package net.primal.android.networking.sockets

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

fun JsonObject.buildNostrREQMessage(subscriptionId: UUID): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.REQ.toString())
        add(subscriptionId.toString())
        add(this@buildNostrREQMessage)
    }.toString()
}

fun JsonObject.buildNostrEVENTMessage(): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.EVENT.toString())
        add(this@buildNostrEVENTMessage)
    }.toString()
}

fun JsonObject.buildNostrAUTHMessage(): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.AUTH.toString())
        add(this@buildNostrAUTHMessage)
    }.toString()
}

fun JsonObject.buildNostrCOUNTMessage(subscriptionId: UUID): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.COUNT.toString())
        add(subscriptionId.toString())
        add(this@buildNostrCOUNTMessage)
    }.toString()
}

fun UUID.buildNostrCLOSEMessage(): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.CLOSE.toString())
        add(this@buildNostrCLOSEMessage.toString())
    }.toString()
}
