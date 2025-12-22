package net.primal.domain.account.model

data class LocalApp(
    val identifier: String,
    val packageName: String,
    val userPubKey: String,
    val trustLevel: TrustLevel,
    val permissions: List<AppPermission>,
) {
    companion object {
        fun identifierOf(packageName: String, userId: String): String = "$packageName:$userId"

        fun userIdFromIdentifier(identifier: String): String = identifier.split(":").last()
    }
}
