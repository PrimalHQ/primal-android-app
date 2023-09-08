package net.primal.android.notifications.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PubkeyRequestBody(
    val pubkey: String,
)
