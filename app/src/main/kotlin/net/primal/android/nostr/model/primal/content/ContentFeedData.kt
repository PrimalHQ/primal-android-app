package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentFeedData(
    val name: String,
    @SerialName("hex") val directive: String,
)
