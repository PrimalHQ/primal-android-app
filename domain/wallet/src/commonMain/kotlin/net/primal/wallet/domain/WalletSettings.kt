package net.primal.wallet.domain

import kotlinx.serialization.Serializable

@Serializable
data class WalletSettings(
    val maxBalanceInBtc: String = "0.01",
    val startInWallet: Boolean = false,
    val spamThresholdAmountInSats: Long = 1,
)
