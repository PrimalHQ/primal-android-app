package net.primal.android.auth.create.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateNostrProfileMetadata(
    val name: String,
    @SerialName("display_name") val displayName: String,
    val about: String,
    val picture: String,
    val banner: String,
    val website: String,
    val lud06: String? = null,
    val lud16: String? = null,
    val nip05: String? = null
)