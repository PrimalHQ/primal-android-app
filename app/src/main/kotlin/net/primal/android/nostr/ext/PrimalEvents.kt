package net.primal.android.nostr.ext

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.serialization.NostrJson

fun PrimalEvent.takeContentAsNostrEventOrNull(): NostrEvent? {
    return try {
        NostrJson.decodeFromJsonElement<NostrEvent>(
            NostrJson.parseToJsonElement(this.content)
        )
    } catch (error: IllegalArgumentException) {
        null
    }
}
