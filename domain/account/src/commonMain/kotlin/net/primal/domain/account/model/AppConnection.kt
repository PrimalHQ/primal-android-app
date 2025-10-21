package net.primal.domain.account.model

data class AppConnection(
    val connectionId: String,
    val userPubKey: String,
    val clientPubKey: String,
    val signerPubKey: String,
    val relays: List<String>,
    val name: String?,
    val url: String?,
    val image: String?,
    val permissions: List<AppPermission>,
)
