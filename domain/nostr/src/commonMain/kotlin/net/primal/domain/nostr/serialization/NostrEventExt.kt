package net.primal.domain.nostr.serialization

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.primal.domain.nostr.NostrEvent

fun NostrEvent.toNostrJsonObject(): JsonObject {
    val nostrEvent = this@toNostrJsonObject
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

fun List<NostrEvent>.toNostrJsonArray(): JsonArray {
    return buildJsonArray {
        forEach {
            add(it.toNostrJsonObject())
        }
    }
}
