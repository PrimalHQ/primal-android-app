package net.primal.data.account.remote.command.model

import kotlinx.serialization.Serializable

@Serializable
data class NostrCommandResponse(
    val id: String,
    val result: String,
    val error: String?,
)
