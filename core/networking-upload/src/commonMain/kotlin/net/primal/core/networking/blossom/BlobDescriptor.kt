package net.primal.core.networking.blossom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlobDescriptor(
    val url: String,
    val sha256: String,
    @SerialName("size") val sizeInBytes: Long,
    val type: String? = null,
    @SerialName("created") val uploaded: Long,
)
