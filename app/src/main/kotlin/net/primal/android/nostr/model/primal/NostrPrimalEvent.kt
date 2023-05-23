package net.primal.android.nostr.model.primal

import kotlinx.serialization.Serializable

@Serializable
data class NostrPrimalEvent(
    val kind: Int,
    val content: String,
)
