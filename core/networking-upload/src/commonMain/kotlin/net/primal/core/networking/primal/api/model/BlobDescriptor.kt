package net.primal.core.networking.primal.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BlobDescriptor(
    val url: String,
    val sha256: String,
    val size: Int,
    val type: String? = null,
    val uploaded: Long,
)
