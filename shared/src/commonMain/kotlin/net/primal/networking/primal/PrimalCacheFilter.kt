package net.primal.networking.primal

import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.data.remote.PrimalVerb
import net.primal.data.serialization.NostrJson

data class PrimalCacheFilter(
    val primalVerb: PrimalVerb? = null,
    val optionsJson: String? = null,
) {
    fun toPrimalJsonObject() =
        buildJsonObject {
            put(
                "cache",
                buildJsonArray {
                    add(primalVerb?.identifier)
                    if (optionsJson != null) {
                        add(NostrJson.decodeFromString(optionsJson))
                    }
                },
            )
        }
}
