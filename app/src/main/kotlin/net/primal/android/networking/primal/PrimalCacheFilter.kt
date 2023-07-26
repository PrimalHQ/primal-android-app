package net.primal.android.networking.primal

import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.android.serialization.NostrJson

data class PrimalCacheFilter(
    val primalVerb: String? = null,
    val optionsJson: String? = null,
) {
    fun toPrimalJsonObject() = buildJsonObject {
        put("cache", buildJsonArray {
            add(primalVerb)
            if (optionsJson != null) {
                add(NostrJson.decodeFromString(optionsJson))
            }
        })
    }
}
