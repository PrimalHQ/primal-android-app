package net.primal.android.nostr.ext

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
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
