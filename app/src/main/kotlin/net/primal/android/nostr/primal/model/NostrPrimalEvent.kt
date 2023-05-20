package net.primal.android.nostr.primal.model

import kotlinx.serialization.Serializable

@Serializable
data class NostrPrimalEvent(
    val kind: Int,
    val content: String,
)
