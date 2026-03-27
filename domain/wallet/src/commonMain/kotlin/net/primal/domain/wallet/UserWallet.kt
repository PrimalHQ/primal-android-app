package net.primal.domain.wallet

data class UserWallet(
    val wallet: Wallet,
    val lightningAddress: String?,
)
