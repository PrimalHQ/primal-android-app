package net.primal.android.nostr.ext

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonPrimitive

fun List<JsonArray>.findPostId(): String? {
    val postTag = firstOrNull { it.isEventIdTag() }
    return postTag?.getTagValueOrNull()
}

fun List<JsonArray>.findPostAuthorId(): String? {
    val postAuthorTag = firstOrNull { it.isPubKeyTag() }
    return postAuthorTag?.getTagValueOrNull()
}

fun JsonArray.isEventIdTag() = getOrNull(0)?.jsonPrimitive?.content == "e"

fun JsonArray.isPubKeyTag() = getOrNull(0)?.jsonPrimitive?.content == "p"

fun JsonArray.getTagValueOrNull() = getOrNull(1)?.jsonPrimitive?.content

fun JsonArray.hasMentionMarker() = contains(JsonPrimitive("mention"))

fun JsonArray.hasReplyMarker() = contains(JsonPrimitive("reply"))

fun JsonArray.hasRootMarker() = contains(JsonPrimitive("root"))

fun JsonArray.hasAnyMarker() = hasRootMarker() || hasReplyMarker() || hasMentionMarker()

fun String.asEventIdTag(recommendedRelay: String = "", marker: String? = null): JsonArray =
    buildJsonArray {
        add("e")
        add(this@asEventIdTag)
        add(recommendedRelay)
        if (marker != null) add(marker)
    }

fun String.asPubkeyTag(recommendedRelay: String = "", marker: String? = null): JsonArray =
    buildJsonArray {
        add("p")
        add(this@asPubkeyTag)
        add(recommendedRelay)
        if (marker != null) add(marker)
    }

fun String.asIdentifierTag(): JsonArray = buildJsonArray {
    add("d")
    add(this@asIdentifierTag)
}

fun String.parseEventTags(marker: String? = null): List<JsonArray> {
    val nostrUris = parseNostrUris()
    if (nostrUris.isEmpty()) return emptyList()

    val tags = mutableListOf<JsonArray>()
    nostrUris.forEach {
        when {
            it.isNEventUri() -> {
                val result = it.nostrUriToNoteIdAndRelay()
                val eventId = result.first
                val relayUrl = result.second
                tags.add(
                    buildJsonArray {
                        add("e")
                        add(eventId)
                        add(relayUrl)
                        if (marker != null) add(marker)
                    }
                )
            }

            it.isNoteUri() || it.isNote() -> tags.add(
                buildJsonArray {
                    add("e")
                    add(it.nostrUriToNoteId())
                    add("")
                    if (marker != null) add(marker)
                }
            )
        }
    }
    return tags.toList()
}
