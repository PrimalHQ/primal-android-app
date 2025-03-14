package net.primal.domain.nostr

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive

data class Naddr(
    val kind: Int,
    val userId: String,
    val identifier: String,
    val relays: List<String> = emptyList(),
)

@Suppress("MagicNumber")
fun JsonArray.aTagToNaddr(): Naddr? {
    return if (this.isATag()) {
        val value = getOrNull(1)?.jsonPrimitive?.content
        val chunks = value?.split(":")
        val kind = chunks?.getOrNull(0)?.toIntOrNull()
        if (chunks?.size == 3 && kind != null) {
            val userId = chunks[1]
            val identifier = chunks[2]
            val relay = getOrNull(2)?.jsonPrimitive?.content
            Naddr(
                kind = kind,
                userId = userId,
                identifier = identifier,
                relays = if (relay?.isNotEmpty() == true) listOf(relay) else emptyList(),
            )
        } else {
            null
        }
    } else {
        null
    }
}

fun Naddr.asATagValue() = "${this.kind}:${this.userId}:${this.identifier}"
