package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.wallet.NostrWalletKeypair
import net.primal.domain.wallet.Wallet as WalletDO
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.ActiveWallet
import net.primal.wallet.data.local.dao.Wallet as WalletPO

fun WalletPO.toDomain(): WalletDO =
    when (this.info.type) {
        WalletType.PRIMAL ->
            WalletDO.Primal(
                walletId = info.walletId,
                userId = info.userId,
                lightningAddress = info.lightningAddress,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats ?: 1L,
                balanceInBtc = info.balanceInBtc,
                maxBalanceInBtc = info.maxBalanceInBtc,
                lastUpdatedAt = info.lastUpdatedAt,
                kycLevel = primal?.kycLevel ?: WalletKycLevel.None,
            )

        WalletType.NWC ->
            WalletDO.NWC(
                walletId = info.walletId,
                userId = info.userId,
                lightningAddress = info.lightningAddress,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats ?: 1L,
                balanceInBtc = info.balanceInBtc,
                maxBalanceInBtc = info.maxBalanceInBtc,
                lastUpdatedAt = info.lastUpdatedAt,
                relays = nwc?.relays ?: emptyList(),
                keypair = NostrWalletKeypair(
                    privateKey = nwc?.walletPrivateKey ?: "",
                    pubKey = nwc?.walletPubkey ?: "",
                ),
            )
    }

fun ActiveWallet.toDomain(): WalletDO? {
    val info = this.info
    if (info == null) return null

    return when (info.type) {
        WalletType.PRIMAL ->
            WalletDO.Primal(
                walletId = info.walletId,
                userId = info.userId,
                lightningAddress = info.lightningAddress,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats ?: 1L,
                balanceInBtc = info.balanceInBtc,
                maxBalanceInBtc = info.maxBalanceInBtc,
                lastUpdatedAt = info.lastUpdatedAt,
                kycLevel = primal?.kycLevel ?: WalletKycLevel.None,
            )

        WalletType.NWC ->
            WalletDO.NWC(
                walletId = info.walletId,
                userId = info.userId,
                lightningAddress = info.lightningAddress,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats ?: 1L,
                balanceInBtc = info.balanceInBtc,
                maxBalanceInBtc = info.maxBalanceInBtc,
                lastUpdatedAt = info.lastUpdatedAt,
                relays = nwc?.relays ?: emptyList(),
                keypair = NostrWalletKeypair(
                    privateKey = nwc?.walletPrivateKey ?: "",
                    pubKey = nwc?.walletPubkey ?: "",
                ),
            )
    }
}
