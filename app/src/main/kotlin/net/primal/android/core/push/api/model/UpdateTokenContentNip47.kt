package net.primal.android.core.push.api.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTokenContentNip47(
    val token: String,
    val relays: Set<String>,
    val clientPubKeys: List<String>,
)
