package net.primal.android.nostr.ext

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

fun PrimalEvent.takeContentAsNostrEventOrNull(): NostrEvent? {
    return try {
        NostrJson.decodeFromJsonElement<NostrEvent>(
            NostrJson.parseToJsonElement(this.content),
        )
    } catch (error: IllegalArgumentException) {
        null
    }
}
