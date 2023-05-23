package net.primal.android.nostr.model.primal.response

import kotlinx.serialization.Serializable

@Serializable
data class FeedData(
    val npub: String,
    val name: String,
    val hex: String,
)