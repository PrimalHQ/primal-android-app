package net.primal.android.nostr.notary

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

fun String.asEventIdTag(): JsonArray = buildJsonArray {
    add("e")
    add(this@asEventIdTag)
}

fun String.asPubkeyTag(): JsonArray = buildJsonArray {
    add("p")
    add(this@asPubkeyTag)
}

fun String.asIdentifierTag(): JsonArray = buildJsonArray {
    add("d")
    add(this@asIdentifierTag)
}
