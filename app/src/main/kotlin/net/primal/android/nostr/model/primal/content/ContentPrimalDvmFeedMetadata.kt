package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ContentPrimalDvmFeedMetadata(
    val amount: String? = null,
    val personalized: Boolean? = null,
    val picture: String? = null,
    val image: String? = null,
    val cashuAccepted: Boolean? = null,
    val nip90Params: JsonObject? = null,
    val encryptionSupported: Boolean? = null,
    val lud16: String? = null,
    val name: String? = null,
    val about: String? = null,
    val subscription: Boolean? = null,
)
