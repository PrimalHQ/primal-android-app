package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Deprecated("Use new feeds.")
@Serializable
data class ContentFeedData(
    val name: String,
    @SerialName("hex") val directive: String,
    val includeReplies: Boolean? = null,
)
