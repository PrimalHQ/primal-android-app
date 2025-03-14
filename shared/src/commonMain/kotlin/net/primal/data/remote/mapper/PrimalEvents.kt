package net.primal.data.remote.mapper

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.data.serialization.NostrJson
import net.primal.domain.PrimalEvent

inline fun <reified T> PrimalEvent?.takeContentOrNull(): T? {
    if (this == null) return null

    return try {
        NostrJson.decodeFromJsonElement<T>(
            NostrJson.parseToJsonElement(this.content),
        )
    } catch (_: IllegalArgumentException) {
        null
    }
}
