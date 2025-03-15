package net.primal.data.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.primal.domain.nostr.NostrEvent

val NostrJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

val NostrJsonImplicitNulls = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

val NostrJsonEncodeDefaults = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

val NostrNotaryJson = Json {
    ignoreUnknownKeys = true
}

fun NostrEvent.toJsonObject(): JsonObject {
    val nostrEvent = this@toJsonObject
    return buildJsonObject {
        put("id", nostrEvent.id)
        put("pubkey", nostrEvent.pubKey)
        put("created_at", nostrEvent.createdAt)
        put("kind", nostrEvent.kind)
        putJsonArray("tags") {
            nostrEvent.tags.forEach { add(it) }
        }
        put("content", nostrEvent.content)
        put("sig", nostrEvent.sig)
    }
}

fun List<NostrEvent>.toJsonArray(): JsonArray {
    return buildJsonArray {
        forEach {
            add(it.toJsonObject())
        }
    }
}

//fun List<Relay>.toNostrRelayMap(): Map<String, RelayPermission> {
//    val map = mutableMapOf<String, RelayPermission>()
//    this.forEach { map[it.url] = RelayPermission(read = it.read, write = it.write) }
//    return map
//}
