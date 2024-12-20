package net.primal.android.nostr.ext

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.utils.parseHashtags
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.nostr.model.NostrEventKind

fun List<JsonArray>.findFirstEventId() = firstOrNull { it.isEventIdTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstProfileId() = firstOrNull { it.isPubKeyTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstZapRequest() = firstOrNull { it.isDescriptionTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstZapAmount() = firstOrNull { it.isAmountTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstBolt11() = firstOrNull { it.isBolt11Tag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstTitle() = firstOrNull { it.isTitleTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstSummary() = firstOrNull { it.isSummaryTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstImage() = firstOrNull { it.isImageTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstPublishedAt() = firstOrNull { it.isPublishedAtTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstIdentifier() = firstOrNull { it.isIdentifierTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstAltDescription() = firstOrNull { it.isAltTag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstReplaceableEventId() = firstOrNull { it.isATag() }?.getTagValueOrNull()

fun List<JsonArray>.findFirstContextTag() = firstOrNull { it.isContextTag() }?.getTagValueOrNull()

fun JsonArray.isContextTag() = getOrNull(0)?.jsonPrimitive?.content == "context"

fun JsonArray.isBolt11Tag() = getOrNull(0)?.jsonPrimitive?.content == "bolt11"

fun JsonArray.isDescriptionTag() = getOrNull(0)?.jsonPrimitive?.content == "description"

fun JsonArray.isAmountTag() = getOrNull(0)?.jsonPrimitive?.content == "amount"

fun JsonArray.isEventIdTag() = getOrNull(0)?.jsonPrimitive?.content == "e"

fun JsonArray.isPubKeyTag() = getOrNull(0)?.jsonPrimitive?.content == "p"

fun JsonArray.isHashtagTag() = getOrNull(0)?.jsonPrimitive?.content == "t"

fun JsonArray.isIdentifierTag() = getOrNull(0)?.jsonPrimitive?.content == "d"

fun JsonArray.isATag() = getOrNull(0)?.jsonPrimitive?.content == "a"

fun JsonArray.isTitleTag() = getOrNull(0)?.jsonPrimitive?.content == "title"

fun JsonArray.isSummaryTag() = getOrNull(0)?.jsonPrimitive?.content == "summary"

fun JsonArray.isImageTag() = getOrNull(0)?.jsonPrimitive?.content == "image"

fun JsonArray.isAltTag() = getOrNull(0)?.jsonPrimitive?.content == "alt"

fun JsonArray.isPublishedAtTag() = getOrNull(0)?.jsonPrimitive?.content == "published_at"

fun JsonArray.getTagValueOrNull() = getOrNull(1)?.jsonPrimitive?.content

fun JsonArray.hasMentionMarker() = contains(JsonPrimitive("mention"))

fun JsonArray.hasReplyMarker() = contains(JsonPrimitive("reply"))

fun JsonArray.hasRootMarker() = contains(JsonPrimitive("root"))

fun String.asContextTag() =
    buildJsonArray {
        add("context")
        add(this@asContextTag)
    }

fun String.asAltTag() =
    buildJsonArray {
        add("alt")
        add(this@asAltTag)
    }

fun NostrEventKind.asKindTag(): JsonArray =
    buildJsonArray {
        add("k")
        add(this@asKindTag.value.toString())
    }

fun String.asEventIdTag(relayHint: String? = null, marker: String? = null): JsonArray =
    buildJsonArray {
        add("e")
        add(this@asEventIdTag)
        if (relayHint != null) add(relayHint)
        if (marker != null) {
            if (relayHint == null) add("")
            add(marker)
        }
    }

fun String.asPubkeyTag(optional: String? = null): JsonArray =
    buildJsonArray {
        add("p")
        add(this@asPubkeyTag)
        if (optional != null) add(optional)
    }

fun String.asIdentifierTag(): JsonArray =
    buildJsonArray {
        add("d")
        add(this@asIdentifierTag)
    }

fun String.asReplaceableEventTag(relayHint: String? = null, marker: String? = null): JsonArray =
    buildJsonArray {
        add("a")
        add(this@asReplaceableEventTag)
        if (relayHint != null) add(relayHint)
        if (marker != null) {
            if (relayHint == null) add("")
            add(marker)
        }
    }

fun NoteAttachment.asIMetaTag(): JsonArray {
    require(this.remoteUrl != null)
    return buildJsonArray {
        add("imeta")
        add("url ${this@asIMetaTag.remoteUrl}")
        this@asIMetaTag.mimeType?.let { add("m $it") }
        this@asIMetaTag.uploadedHash?.let { add("x $it") }
        this@asIMetaTag.originalHash?.let { add("ox $it") }
        this@asIMetaTag.uploadedSizeInBytes?.let { add("size $it") }
        this@asIMetaTag.dimensionInPixels?.let { add("dim $it") }
    }
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
                    },
                )
            }

            it.isNoteUri() || it.isNote() -> tags.add(
                buildJsonArray {
                    add("e")
                    add(it.nostrUriToNoteId())
                    add("")
                    if (marker != null) add(marker)
                },
            )
        }
    }
    return tags.toList()
}

fun String.parsePubkeyTags(marker: String? = null): List<JsonArray> {
    val nostrUris = parseNostrUris()
    if (nostrUris.isEmpty()) return emptyList()

    val tags = mutableListOf<JsonArray>()
    nostrUris.forEach {
        when {
            it.isNProfileUri() || it.isNProfile() -> {
                val result = it.nostrUriToPubkeyAndRelay()
                val pubkey = result.first
                val relayUrl = result.second
                tags.add(
                    buildJsonArray {
                        add("p")
                        add(pubkey)
                        add(relayUrl)
                        if (marker != null) add(marker)
                    },
                )
            }

            it.isNPubUri() || it.isNPub() -> tags.add(
                buildJsonArray {
                    add("p")
                    add(it.nostrUriToPubkey())
                    add("")
                    if (marker != null) add(marker)
                },
            )
        }
    }
    return tags.toList()
}

fun String.parseHashtagTags(): List<JsonArray> {
    val hashtags = parseHashtags()
    val tags = mutableListOf<JsonArray>()
    hashtags.forEach {
        tags.add(
            buildJsonArray {
                add("t")
                add(it.removePrefix("#"))
            },
        )
    }
    return tags.toList()
}
