package net.primal.domain.account.model

data class LocalApp(
    val identifier: String,
    val packageName: String,
    val userPubKey: String,
    val trustLevel: TrustLevel,
    val permissions: List<AppPermission>,
)
