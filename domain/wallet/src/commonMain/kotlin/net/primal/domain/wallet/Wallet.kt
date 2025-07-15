package net.primal.domain.wallet

sealed class Wallet(
    open val walletId: String,
    open val lightningAddress: String?,
    open val spamThresholdAmountInSats: Long,
    open val balanceInBtc: Double? = null,
    open val maxBalanceInBtc: Double? = null,
    open val lastUpdatedAt: Long? = null,
) {
    data class NWC(
        override val walletId: String,
        override val lightningAddress: String?,
        override val spamThresholdAmountInSats: Long,
        override val balanceInBtc: Double?,
        override val maxBalanceInBtc: Double?,
        override val lastUpdatedAt: Long?,
        val relays: List<String>,
        val keypair: NostrWalletKeypair,
    ) : Wallet(walletId, lightningAddress, spamThresholdAmountInSats, balanceInBtc, maxBalanceInBtc, lastUpdatedAt)

    data class Primal(
        override val walletId: String,
        override val lightningAddress: String?,
        override val spamThresholdAmountInSats: Long,
        override val balanceInBtc: Double?,
        override val maxBalanceInBtc: Double?,
        override val lastUpdatedAt: Long?,
        val kycLevel: WalletKycLevel,
    ) : Wallet(walletId, lightningAddress, spamThresholdAmountInSats, balanceInBtc, maxBalanceInBtc, lastUpdatedAt)
}
