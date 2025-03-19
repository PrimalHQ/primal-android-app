package net.primal.core.networking.sockets

import kotlin.uuid.Uuid
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.core.utils.AppBuildHelper

internal fun JsonObject.buildNostrREQMessage(subscriptionId: String): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.REQ.toString())
        add(subscriptionId)
        add(this@buildNostrREQMessage)
    }.toString()
}

internal fun JsonObject.buildNostrEVENTMessage(): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.EVENT.toString())
        add(this@buildNostrEVENTMessage)
    }.toString()
}

internal fun JsonObject.buildNostrAUTHMessage(): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.AUTH.toString())
        add(this@buildNostrAUTHMessage)
    }.toString()
}

internal fun JsonObject.buildNostrCOUNTMessage(subscriptionId: String): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.COUNT.toString())
        add(subscriptionId)
        add(this@buildNostrCOUNTMessage)
    }.toString()
}

internal fun String.buildNostrCLOSEMessage(): String {
    return buildJsonArray {
        add(NostrVerb.Outgoing.CLOSE.toString())
        add(this@buildNostrCLOSEMessage)
    }.toString()
}

internal fun Uuid.toPrimalSubscriptionId(): String = "${AppBuildHelper.getPlatformName()}-$this".lowercase()
