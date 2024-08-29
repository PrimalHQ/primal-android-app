package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentArticleFeedData(
    val name: String,
    val spec: String,
    @SerialName("feedkind") val feedKind: String,
    val description: String,
    val enabled: Boolean,
)
