package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.wallet.NostrWalletKeypair
import net.primal.domain.wallet.Wallet as WalletDO
import net.primal.domain.wallet.Wallet.NWC
import net.primal.domain.wallet.Wallet.Primal
import net.primal.domain.wallet.Wallet.Tsunami
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.ActiveWallet
import net.primal.wallet.data.local.dao.Wallet as WalletPO

inline fun <reified T : WalletDO> WalletPO.toDomain(): T =
    when (this.info.type) {
        WalletType.PRIMAL ->
            Primal(
                walletId = info.walletId,
                userId = info.userId.decrypted,
                lightningAddress = info.lightningAddress?.decrypted,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats?.decrypted ?: 1L,
                balanceInBtc = info.balanceInBtc?.decrypted,
                maxBalanceInBtc = info.maxBalanceInBtc?.decrypted,
                lastUpdatedAt = info.lastUpdatedAt,
                kycLevel = primal?.kycLevel ?: WalletKycLevel.None,
            )

        WalletType.NWC ->
            NWC(
                walletId = info.walletId,
                userId = info.userId.decrypted,
                lightningAddress = info.lightningAddress?.decrypted,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats?.decrypted ?: 1L,
                balanceInBtc = info.balanceInBtc?.decrypted,
                maxBalanceInBtc = info.maxBalanceInBtc?.decrypted,
                lastUpdatedAt = info.lastUpdatedAt,
                relays = nwc?.relays?.decrypted ?: emptyList(),
                pubkey = nwc?.pubkey?.decrypted ?: "",
                keypair = NostrWalletKeypair(
                    privateKey = nwc?.walletPrivateKey?.decrypted ?: "",
                    pubkey = nwc?.walletPubkey?.decrypted ?: "",
                ),
            )

        WalletType.TSUNAMI -> {
            Tsunami(
                walletId = info.walletId,
                userId = info.userId.decrypted,
                lightningAddress = info.lightningAddress?.decrypted,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats?.decrypted ?: 1L,
                balanceInBtc = info.balanceInBtc?.decrypted,
                maxBalanceInBtc = info.maxBalanceInBtc?.decrypted,
                lastUpdatedAt = info.lastUpdatedAt,
            )
        }
    } as T

fun ActiveWallet.toDomain(): WalletDO? {
    val info = this.info
    if (info == null) return null

    return when (info.type) {
        WalletType.PRIMAL ->
            Primal(
                walletId = info.walletId,
                userId = info.userId.decrypted,
                lightningAddress = info.lightningAddress?.decrypted,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats?.decrypted ?: 1L,
                balanceInBtc = info.balanceInBtc?.decrypted,
                maxBalanceInBtc = info.maxBalanceInBtc?.decrypted,
                lastUpdatedAt = info.lastUpdatedAt,
                kycLevel = primal?.kycLevel ?: WalletKycLevel.None,
            )

        WalletType.NWC ->
            NWC(
                walletId = info.walletId,
                userId = info.userId.decrypted,
                lightningAddress = info.lightningAddress?.decrypted,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats?.decrypted ?: 1L,
                balanceInBtc = info.balanceInBtc?.decrypted,
                maxBalanceInBtc = info.maxBalanceInBtc?.decrypted,
                lastUpdatedAt = info.lastUpdatedAt,
                relays = nwc?.relays?.decrypted ?: emptyList(),
                pubkey = nwc?.pubkey?.decrypted ?: "",
                keypair = NostrWalletKeypair(
                    privateKey = nwc?.walletPrivateKey?.decrypted ?: "",
                    pubkey = nwc?.walletPubkey?.decrypted ?: "",
                ),
            )

        WalletType.TSUNAMI ->
            Tsunami(
                walletId = info.walletId,
                userId = info.userId.decrypted,
                lightningAddress = info.lightningAddress?.decrypted,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats?.decrypted ?: 1L,
                balanceInBtc = info.balanceInBtc?.decrypted,
                maxBalanceInBtc = info.maxBalanceInBtc?.decrypted,
                lastUpdatedAt = info.lastUpdatedAt,
            )
    }
}
