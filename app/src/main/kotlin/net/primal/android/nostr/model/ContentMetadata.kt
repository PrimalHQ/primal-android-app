package net.primal.android.nostr.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentMetadata(
    val name: String? = null,
    val about: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val picture: String? = null,
    val banner: String? = null,
    val website: String? = null,
)
