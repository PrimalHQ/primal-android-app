package net.primal.android.nostr.ext

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.nostr.model.primal.PrimalEvent

inline fun <reified T> PrimalEvent?.takeContentOrNull(): T? {
    if (this == null) return null

    return try {
        NostrJson.decodeFromJsonElement<T>(
            NostrJson.parseToJsonElement(this.content),
        )
    } catch (error: IllegalArgumentException) {
        null
    }
}
