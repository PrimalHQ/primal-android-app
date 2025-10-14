package net.primal.domain.wallet

sealed class Wallet(
    open val walletId: String,
    private val walletType: WalletType,
    open val userId: String,
    open val lightningAddress: String?,
    open val spamThresholdAmountInSats: Long,
    open val balanceInBtc: Double? = null,
    open val maxBalanceInBtc: Double? = null,
    open val lastUpdatedAt: Long? = null,
) {
    val type: WalletType get() = walletType

    data class NWC(
        override val walletId: String,
        override val userId: String,
        override val lightningAddress: String?,
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
        userId = userId,
        lightningAddress = lightningAddress,
        spamThresholdAmountInSats = spamThresholdAmountInSats,
        balanceInBtc = balanceInBtc,
        maxBalanceInBtc = maxBalanceInBtc,
        lastUpdatedAt = lastUpdatedAt,
    )

    data class Primal(
        override val walletId: String,
        override val userId: String,
        override val lightningAddress: String?,
        override val spamThresholdAmountInSats: Long,
        override val balanceInBtc: Double?,
        override val maxBalanceInBtc: Double?,
        override val lastUpdatedAt: Long?,
        val kycLevel: WalletKycLevel,
    ) : Wallet(
        walletId = walletId,
        walletType = WalletType.PRIMAL,
        userId = userId,
        lightningAddress = lightningAddress,
        spamThresholdAmountInSats = spamThresholdAmountInSats,
        balanceInBtc = balanceInBtc,
        maxBalanceInBtc = maxBalanceInBtc,
        lastUpdatedAt = lastUpdatedAt,
    )

    data class Tsunami(
        override val walletId: String,
        override val userId: String,
        override val lightningAddress: String?,
        override val spamThresholdAmountInSats: Long,
        override val balanceInBtc: Double?,
        override val maxBalanceInBtc: Double?,
        override val lastUpdatedAt: Long?,
    ) : Wallet(
        walletId = walletId,
        walletType = WalletType.TSUNAMI,
        userId = userId,
        lightningAddress = lightningAddress,
        spamThresholdAmountInSats = spamThresholdAmountInSats,
        balanceInBtc = balanceInBtc,
        maxBalanceInBtc = maxBalanceInBtc,
        lastUpdatedAt = lastUpdatedAt,
    )
}
