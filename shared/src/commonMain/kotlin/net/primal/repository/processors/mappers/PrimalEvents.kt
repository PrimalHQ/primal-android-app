package net.primal.repository.processors.mappers

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.networking.model.primal.PrimalEvent
import net.primal.serialization.json.NostrJson

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
