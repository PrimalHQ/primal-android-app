package net.primal.data.account.remote.command.model

data class NostrCommandResponse(
    val id: String,
    val result: String,
    val error: String?,
)
