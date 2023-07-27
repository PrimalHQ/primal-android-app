package net.primal.android.serialization

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.primal.android.nostr.model.NostrEvent

fun NostrEvent.toJsonObject(): JsonObject {
    val nostrEvent = this@toJsonObject
    return buildJsonObject {
        put("id", nostrEvent.id)
        put("pubkey", nostrEvent.pubKey)
        put("created_at", nostrEvent.createdAt)
        put("kind", nostrEvent.kind)
        putJsonArray("tags") {
            nostrEvent.tags?.forEach { add(it) }
        }
        put("content", nostrEvent.content)
        put("sig", nostrEvent.sig)
    }
}
