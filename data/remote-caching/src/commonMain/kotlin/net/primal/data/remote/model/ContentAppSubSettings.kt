package net.primal.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentAppSubSettings<T>(
    @SerialName("subkey") val key: String,
    @SerialName("settings") val settings: T? = null,
)
