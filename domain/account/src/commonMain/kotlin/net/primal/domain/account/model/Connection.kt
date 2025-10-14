package net.primal.domain.account.model

data class Connection(
    val connectionId: String,
    val relays: List<String>,
    val name: String?,
    val url: String?,
    val image: String?,
)
