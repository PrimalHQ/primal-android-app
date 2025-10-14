package net.primal.data.account.remote.command.model

data class NostrCommandRequest(
    val id: String,
    val method: NostrCommandMethod,
    val params: List<String>,
)
