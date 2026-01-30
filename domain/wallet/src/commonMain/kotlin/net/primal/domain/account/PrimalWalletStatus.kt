package net.primal.domain.account

data class PrimalWalletStatus(
    val hasCustodialWallet: Boolean,
    val hasMigratedToSparkWallet: Boolean,
)
