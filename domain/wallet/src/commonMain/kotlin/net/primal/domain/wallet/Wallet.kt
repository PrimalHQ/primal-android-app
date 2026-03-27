package net.primal.domain.wallet

sealed class Wallet(
    open val walletId: String,
    private val walletType: WalletType,
    open val spamThresholdAmountInSats: Long,
    open val balanceInBtc: Double? = null,
    open val maxBalanceInBtc: Double? = null,
    open val lastUpdatedAt: Long? = null,
) {
    val type: WalletType get() = walletType

    data class NWC(
        override val walletId: String,
        override val spamThresholdAmountInSats: Long,
        override val balanceInBtc: Double?,
        override val maxBalanceInBtc: Double?,
        override val lastUpdatedAt: Long?,
        val pubkey: String,
        val relays: List<String>,
        val keypair: NostrWalletKeypair,
    ) : Wallet(
        walletId = walletId,
        walletType = WalletType.NWC,
        spamThresholdAmountInSats = spamThresholdAmountInSats,
        balanceInBtc = balanceInBtc,
        maxBalanceInBtc = maxBalanceInBtc,
        lastUpdatedAt = lastUpdatedAt,
    )

    data class Primal(
        override val walletId: String,
        override val spamThresholdAmountInSats: Long,
        override val balanceInBtc: Double?,
        override val maxBalanceInBtc: Double?,
        override val lastUpdatedAt: Long?,
        val kycLevel: WalletKycLevel,
    ) : Wallet(
        walletId = walletId,
        walletType = WalletType.PRIMAL,
        spamThresholdAmountInSats = spamThresholdAmountInSats,
        balanceInBtc = balanceInBtc,
        maxBalanceInBtc = maxBalanceInBtc,
        lastUpdatedAt = lastUpdatedAt,
    )

    data class Spark(
        override val walletId: String,
        override val spamThresholdAmountInSats: Long,
        override val balanceInBtc: Double?,
        override val maxBalanceInBtc: Double?,
        override val lastUpdatedAt: Long?,
        val isBackedUp: Boolean,
    ) : Wallet(
        walletId = walletId,
        walletType = WalletType.SPARK,
        spamThresholdAmountInSats = spamThresholdAmountInSats,
        balanceInBtc = balanceInBtc,
        maxBalanceInBtc = maxBalanceInBtc,
        lastUpdatedAt = lastUpdatedAt,
    )
}
