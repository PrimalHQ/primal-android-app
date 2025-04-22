package net.primal.android.nostr.ext

import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.common.PrimalEvent

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
