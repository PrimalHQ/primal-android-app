package net.primal.android.nostr.model.primal

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class PrimalEvent(
    val kind: Int,
    val tags: List<JsonArray> = emptyList(),
    val content: String = "",
)
