package net.primal.android.nostr.model.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentMetadata(
    val name: String? = null,
    val nip05: String? = null,
    val about: String? = null,
    val lud16: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val picture: String? = null,
    val banner: String? = null,
    val website: String? = null,
)

fun ContentMetadata.displayableName(npub: String): String {
    return if (this.displayName == null || this.displayName == "") {
        if (this.name == null || this.name == "") {
            npub
        } else {
            this.name
        }
    } else {
        this.displayName
    }
}
