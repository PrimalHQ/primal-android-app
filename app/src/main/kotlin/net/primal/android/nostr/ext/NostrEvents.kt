package net.primal.android.nostr.ext

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.NostrPrimalEvent
import net.primal.android.serialization.NostrJson

fun JsonObject?.asNostrEventOrNull(): NostrEvent? {
    return try {
        if (this != null) NostrJson.decodeFromJsonElement(this) else null
    } catch (error: IllegalArgumentException) {
        null
    }
}

fun JsonObject?.asNostrPrimalEventOrNull(): NostrPrimalEvent? {
    return try {
        if (this != null) NostrJson.decodeFromJsonElement(this) else null
    } catch (error: IllegalArgumentException) {
        null
    }
}
