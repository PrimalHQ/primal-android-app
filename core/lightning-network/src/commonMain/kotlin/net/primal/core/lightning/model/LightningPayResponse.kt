package net.primal.core.lightning.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LightningPayResponse(
    @SerialName("pr") val invoice: String,
    val routes: List<String> = emptyList(),
)
