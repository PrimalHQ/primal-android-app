package net.primal.data.account.remote.command.model

import kotlinx.serialization.Serializable

@Serializable
data class NostrCommandRequest(
    val id: String,
    val method: NostrCommandMethod,
    val params: List<String>,
)
