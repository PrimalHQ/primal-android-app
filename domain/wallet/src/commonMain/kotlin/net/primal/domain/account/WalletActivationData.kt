package net.primal.domain.account

data class WalletActivationData(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val dateOfBirth: Long? = null,
    val country: Region? = null,
    val state: Region? = null,
)
