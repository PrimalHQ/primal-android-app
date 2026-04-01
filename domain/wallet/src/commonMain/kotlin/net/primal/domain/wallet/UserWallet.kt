package net.primal.domain.wallet

data class UserWallet(
    val userId: String,
    val wallet: Wallet,
    val lightningAddress: String?,
)
