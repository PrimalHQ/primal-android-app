package net.primal.android.nostr.model.primal.response

import kotlinx.serialization.Serializable

@Serializable
data class FeedData(
    val name: String,
    val hex: String,
)