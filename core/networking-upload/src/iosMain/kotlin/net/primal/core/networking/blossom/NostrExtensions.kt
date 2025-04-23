package net.primal.core.networking.blossom

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.SignResult

object NostrExtensions {

    fun mapAsJsonArray(tag: List<String>): JsonArray {
        return buildJsonArray {
            tag.forEach {
                add(it)
            }
        }
    }

    fun mapAsListOfJsonArray(tags: List<List<String>>): List<JsonArray> {
        return tags.map { mapAsJsonArray(it) }
    }

    fun mapAsListOfStrings(tag: JsonArray): List<String> {
        return tag.map { it.jsonPrimitive.content }
    }

    fun mapAsListOfListOfStrings(tags: List<JsonArray>): List<List<String>> {
        return tags.map { mapAsListOfStrings(it) }
    }

    fun buildNostrSignResult(
        id: String,
        pubKey: String,
        createdAt: Long,
        kind: Int,
        tags: List<List<String>> = emptyList(),
        content: String,
        sig: String,
    ): SignResult.Signed {
        return SignResult.Signed(
            event = NostrEvent(
                id = id,
                pubKey = pubKey,
                createdAt = createdAt,
                kind = kind,
                tags = mapAsListOfJsonArray(tags),
                content = content,
                sig = sig,
            ),
        )
    }
}
