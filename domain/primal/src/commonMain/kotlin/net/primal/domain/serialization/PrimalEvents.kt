package net.primal.domain.serialization

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.PrimalEvent

inline fun <reified T> PrimalEvent?.takeContentOrNull(): T? {
    if (this == null) return null

    return try {
        CommonJson.decodeFromJsonElement<T>(
            CommonJson.parseToJsonElement(this.content),
        )
    } catch (_: IllegalArgumentException) {
        null
    }
}
