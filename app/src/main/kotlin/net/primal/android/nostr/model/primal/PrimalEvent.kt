package net.primal.android.nostr.model.primal

import kotlinx.serialization.Serializable

@Serializable
data class PrimalEvent(
    val kind: Int,
    val content: String,
)
