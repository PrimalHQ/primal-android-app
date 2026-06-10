package net.primal.domain.account

data class PrimalWalletStatus(
    val hasMigratedToSparkWallet: Boolean,
    val lightningAddress: String? = null,
    val registeredSparkWalletId: String?,
)
