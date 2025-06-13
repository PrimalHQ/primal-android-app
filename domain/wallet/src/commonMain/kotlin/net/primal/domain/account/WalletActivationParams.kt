package net.primal.domain.account

data class WalletActivationParams(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    /** yyyy-MM-dd */
    val dateOfBirth: String,
    val country: String,
    val state: String,
)
