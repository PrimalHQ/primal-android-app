package net.primal.networking.sockets

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.PrimalLib

fun JsonObject.buildNostrREQMessage(subscriptionId: String): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.REQ.toString())
        add(subscriptionId)
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

fun JsonObject.buildNostrCOUNTMessage(subscriptionId: String): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.COUNT.toString())
        add(subscriptionId)
        add(this@buildNostrCOUNTMessage)
    }.toString()
}

fun String.buildNostrCLOSEMessage(): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.CLOSE.toString())
        add(this@buildNostrCLOSEMessage)
    }.toString()
}

@OptIn(ExperimentalUuidApi::class)
fun Uuid.toPrimalSubscriptionId(): String = "${PrimalLib.appName}-$this"
