package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentArticleFeedData(
    val name: String,
    val spec: String,
)
