package net.primal.android.nostr.model.zap

import kotlinx.serialization.Serializable

@Serializable
data class LightningPayRequest(
    val allowsNostr: Boolean,
    val commentsAllowed: Boolean,
    val callback: String
)
