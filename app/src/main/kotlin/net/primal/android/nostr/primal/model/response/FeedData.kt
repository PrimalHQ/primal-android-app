package net.primal.android.nostr.primal.model.response

import kotlinx.serialization.Serializable

@Serializable
data class FeedData(
    val npub: String,
    val name: String,
    val hex: String,
)