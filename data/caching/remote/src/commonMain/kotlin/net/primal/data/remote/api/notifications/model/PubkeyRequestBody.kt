package net.primal.data.remote.api.notifications.model

import kotlinx.serialization.Serializable

@Serializable
data class PubkeyRequestBody(
    val pubkey: String,
)
