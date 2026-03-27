package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.wallet.NostrWalletKeypair
import net.primal.domain.wallet.UserWallet
import net.primal.domain.wallet.Wallet as WalletDO
import net.primal.domain.wallet.Wallet.NWC
import net.primal.domain.wallet.Wallet.Primal
import net.primal.domain.wallet.Wallet.Spark
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.ActiveWallet
import net.primal.wallet.data.local.dao.Wallet as WalletPO

inline fun <reified T : WalletDO> WalletPO.toDomain(): T =
    when (this.info.type) {
        WalletType.PRIMAL ->
            Primal(
                walletId = info.walletId,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats?.decrypted ?: 1L,
                balanceInBtc = info.balanceInBtc?.decrypted,
                maxBalanceInBtc = info.maxBalanceInBtc?.decrypted,
                lastUpdatedAt = info.lastUpdatedAt,
                kycLevel = primal?.kycLevel ?: WalletKycLevel.None,
            )

        WalletType.NWC ->
            NWC(
                walletId = info.walletId,
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

        WalletType.SPARK -> {
            Spark(
                walletId = info.walletId,
                spamThresholdAmountInSats = settings?.spamThresholdAmountInSats?.decrypted ?: 1L,
                balanceInBtc = info.balanceInBtc?.decrypted,
                maxBalanceInBtc = info.maxBalanceInBtc?.decrypted,
                lastUpdatedAt = info.lastUpdatedAt,
                isBackedUp = spark?.backedUp ?: false,
            )
        }
    } as T

fun WalletPO.toDomain(userId: String): UserWallet {
    val lightningAddress = links.find { it.userId == userId }?.lightningAddress?.decrypted
    return UserWallet(
        wallet = toDomain(),
        lightningAddress = lightningAddress,
    )
}

fun ActiveWallet.toDomain(): UserWallet? {
    val info = this.info ?: return null
    val walletPO = WalletPO(info = info, links = links, primal = primal, nwc = nwc, spark = spark, settings = settings)
    return walletPO.toDomain(userId = active.userId)
}
