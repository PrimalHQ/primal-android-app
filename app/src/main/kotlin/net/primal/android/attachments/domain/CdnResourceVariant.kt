package net.primal.android.attachments.domain

import kotlinx.serialization.Serializable

@Serializable
data class CdnResourceVariant(
    val width: Int,
    val height: Int,
    val mediaUrl: String,
)
