package net.primal.domain.account.model

data class LocalApp(
    val packageName: String,
    val userPubKey: String,
    val image: String,
    val name: String,
    val trustLevel: TrustLevel,
    val permissions: List<AppPermission>,
)
