package net.primal.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceableEventRequest(
    val pubkey: String,
    val kind: Int,
)
