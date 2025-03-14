package net.primal.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class PrimalEvent(
    val kind: Int,
    val id: String? = null,
    @SerialName("pubkey") val pubKey: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
    val tags: List<JsonArray> = emptyList(),
    val content: String = "",
)
