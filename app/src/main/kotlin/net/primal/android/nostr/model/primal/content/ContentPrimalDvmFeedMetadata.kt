package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ContentPrimalDvmFeedMetadata(
    val amount: String,
    val personalized: Boolean,
    val picture: String,
    val image: String,
    val cashuAccepted: Boolean,
    val nip90Params: JsonObject,
    val encryptionSupported: Boolean,
    val lud16: String,
    val name: String,
    val about: String,
    val subscription: Boolean? = null,
)
