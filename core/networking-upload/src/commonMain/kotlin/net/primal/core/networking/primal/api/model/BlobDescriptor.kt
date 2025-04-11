package net.primal.core.networking.primal.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlobDescriptor(
    val url: String,
    val sha256: String,
    @SerialName("size") val sizeInBytes: Long,
    val type: String? = null,
    val uploaded: Long,
)
