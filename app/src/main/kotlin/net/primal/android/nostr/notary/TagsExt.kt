package net.primal.android.nostr.notary

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

fun String.asEventIdTag(
    recommendedRelay: String = "",
    marker: String? = null,
): JsonArray = buildJsonArray {
    add("e")
    add(this@asEventIdTag)
    add(recommendedRelay)
    if (marker != null) add(marker)
}

fun String.asPubkeyTag(
    recommendedRelay: String = "",
    marker: String? = null,
): JsonArray = buildJsonArray {
    add("p")
    add(this@asPubkeyTag)
    add(recommendedRelay)
    if (marker != null) add(marker)
}

fun String.asIdentifierTag(): JsonArray = buildJsonArray {
    add("d")
    add(this@asIdentifierTag)
}

fun String.asContactTag(): JsonArray = buildJsonArray {
    add("p")
    add(this@asContactTag)
}
