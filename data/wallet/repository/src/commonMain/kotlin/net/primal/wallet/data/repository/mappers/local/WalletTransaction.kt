package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.NostrEntity
import net.primal.domain.nostr.toNostrString
import net.primal.domain.nostr.utils.asNostrEntity
import net.primal.domain.profile.ProfileData
import net.primal.domain.transactions.Transaction as TransactionDO
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.NostrTransactionData
import net.primal.wallet.data.local.dao.PrimalTransactionData
import net.primal.wallet.data.local.dao.WalletTransaction as WalletTransactionPO
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.model.Transaction as TransactionDto

internal fun WalletTransactionPO.toDomain(otherProfile: ProfileData? = null): TransactionDO =
    when (this.info.walletType) {
        WalletType.PRIMAL -> fromPrimalTxToDomain(otherProfile = otherProfile)
        WalletType.NWC -> fromNostrTxToDomain(otherProfile = otherProfile)
        WalletType.TSUNAMI -> throw NotImplementedError() // TODO Handler transactions for tsunami
    }

private fun WalletTransactionPO.fromNostrTxToDomain(otherProfile: ProfileData?) =
    when (val zappedEntity = this.info.zappedEntity?.decrypted?.asNostrEntity()?.getOrNull()) {
        is Naddr, is Nevent -> {
            TransactionDO.Zap(
                transactionId = this.info.transactionId,
                walletId = this.info.walletId,
                walletType = this.info.walletType,
                type = this.info.type,
                state = this.info.state,
                createdAt = this.info.createdAt.decrypted,
                updatedAt = this.info.updatedAt,
                completedAt = this.info.completedAt?.decrypted,
                userId = this.info.userId.decrypted,
                note = this.info.note?.decrypted,
                invoice = this.info.invoice?.decrypted,
                amountInBtc = this.info.amountInBtc.decrypted,
                amountInUsd = this.primal?.amountInUsd?.decrypted,
                exchangeRate = this.primal?.exchangeRate?.decrypted,
                totalFeeInBtc = this.info.totalFeeInBtc?.decrypted,
                otherUserId = this.info.otherUserId?.decrypted,
                otherLightningAddress = this.primal?.otherLightningAddress?.decrypted,
                zappedEntity = zappedEntity,
                zappedByUserId = this.info.zappedByUserId?.decrypted,
                otherUserProfile = otherProfile,
            )
        }

        else -> {
            TransactionDO.Lightning(
                transactionId = this.info.transactionId,
                walletId = this.info.walletId,
                walletType = this.info.walletType,
                type = this.info.type,
                state = this.info.state,
                createdAt = this.info.createdAt.decrypted,
                updatedAt = this.info.updatedAt,
                completedAt = this.info.completedAt?.decrypted,
                userId = this.info.userId.decrypted,
                note = this.info.note?.decrypted,
                invoice = this.info.invoice?.decrypted,
                amountInBtc = this.info.amountInBtc.decrypted,
                amountInUsd = this.primal?.amountInUsd?.decrypted,
                exchangeRate = this.primal?.exchangeRate?.decrypted,
                totalFeeInBtc = this.info.totalFeeInBtc?.decrypted,
                otherUserId = this.info.otherUserId?.decrypted,
                otherLightningAddress = this.primal?.otherLightningAddress?.decrypted,
                otherUserProfile = otherProfile,
            )
        }
    }

private fun WalletTransactionPO.fromPrimalTxToDomain(otherProfile: ProfileData?): TransactionDO {
    val zappedEntity = this.info.zappedEntity?.decrypted?.asNostrEntity()?.getOrNull()

    return when {
        this.primal?.isZap == true && zappedEntity != null
        -> this.asZapTransactionDO(otherProfile = otherProfile, zappedEntity = zappedEntity)

        this.primal?.isStorePurchase == true -> this.asStorePurchaseTransactionDO()

        this.primal?.onChainAddress != null -> this.asOnChainTransactionDO()

        else -> this.asLightningTransaction(otherProfile = otherProfile)
    }
}

internal fun TransactionDto.toWalletTransactionData() =
    WalletTransactionData(
        transactionId = this.transactionId,
        walletId = this.walletId,
        walletType = this.walletType,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt.asEncryptable(),
        updatedAt = this.updatedAt,
        completedAt = this.completedAt?.asEncryptable(),
        amountInBtc = this.amountInBtc.asEncryptable(),
        userId = this.userId.asEncryptable(),
        note = this.note?.asEncryptable(),
        invoice = this.invoice?.asEncryptable(),
        totalFeeInBtc = this.totalFeeInBtc?.asEncryptable(),
        zappedEntity = this.zappedEntity?.toNostrString()?.asEncryptable(),
        zappedByUserId = this.zappedByUserId?.asEncryptable(),
        otherUserId = this.otherUserId?.asEncryptable(),
    )

internal fun TransactionDto.Primal.toPrimalTransactionData() =
    PrimalTransactionData(
        transactionId = this.transactionId,
        walletLightningAddress = this.walletLightningAddress.asEncryptable(),
        amountInUsd = this.amountInUsd?.asEncryptable(),
        isZap = this.isZap,
        isStorePurchase = this.isStorePurchase,
        userSubWallet = this.userSubWallet,
        userLightningAddress = this.userLightningAddress?.asEncryptable(),
        otherLightningAddress = this.otherLightningAddress?.asEncryptable(),
        exchangeRate = this.exchangeRate?.asEncryptable(),
        onChainAddress = this.onChainAddress?.asEncryptable(),
        onChainTxId = this.onChainTxId?.asEncryptable(),
    )

internal fun TransactionDto.NWC.toNostrTransactionData() =
    NostrTransactionData(
        transactionId = this.transactionId,
        preimage = this.preimage?.asEncryptable(),
        descriptionHash = this.descriptionHash?.asEncryptable(),
        paymentHash = this.paymentHash?.asEncryptable(),
        metadata = this.metadata?.asEncryptable(),
    )

private fun WalletTransactionPO.asZapTransactionDO(otherProfile: ProfileData?, zappedEntity: NostrEntity) =
    TransactionDO.Zap(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt.decrypted,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt?.decrypted,
        userId = this.info.userId.decrypted,
        note = this.info.note?.decrypted,
        invoice = this.info.invoice?.decrypted,
        amountInBtc = this.info.amountInBtc.decrypted,
        amountInUsd = this.primal?.amountInUsd?.decrypted,
        exchangeRate = this.primal?.exchangeRate?.decrypted,
        totalFeeInBtc = this.info.totalFeeInBtc?.decrypted,
        otherUserId = this.info.otherUserId?.decrypted,
        otherLightningAddress = this.primal?.otherLightningAddress?.decrypted,
        zappedEntity = zappedEntity,
        zappedByUserId = this.info.zappedByUserId?.decrypted,
        otherUserProfile = otherProfile,
    )

private fun WalletTransactionPO.asStorePurchaseTransactionDO() =
    TransactionDO.StorePurchase(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt.decrypted,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt?.decrypted,
        userId = this.info.userId.decrypted,
        note = this.info.note?.decrypted,
        invoice = this.info.invoice?.decrypted,
        amountInBtc = this.info.amountInBtc.decrypted,
        amountInUsd = this.primal?.amountInUsd?.decrypted,
        exchangeRate = this.primal?.exchangeRate?.decrypted,
        totalFeeInBtc = this.info.totalFeeInBtc?.decrypted,
    )

private fun WalletTransactionPO.asOnChainTransactionDO() =
    TransactionDO.OnChain(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt.decrypted,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt?.decrypted,
        userId = this.info.userId.decrypted,
        note = this.info.note?.decrypted,
        invoice = this.info.invoice?.decrypted,
        amountInBtc = this.info.amountInBtc.decrypted,
        amountInUsd = this.primal?.amountInUsd?.decrypted,
        exchangeRate = this.primal?.exchangeRate?.decrypted,
        totalFeeInBtc = this.info.totalFeeInBtc?.decrypted,
        onChainTxId = this.primal?.onChainTxId?.decrypted,
        onChainAddress = this.primal?.onChainAddress?.decrypted,
    )

private fun WalletTransactionPO.asLightningTransaction(otherProfile: ProfileData?) =
    TransactionDO.Lightning(
        transactionId = this.info.transactionId,
        walletId = this.info.walletId,
        walletType = this.info.walletType,
        type = this.info.type,
        state = this.info.state,
        createdAt = this.info.createdAt.decrypted,
        updatedAt = this.info.updatedAt,
        completedAt = this.info.completedAt?.decrypted,
        userId = this.info.userId.decrypted,
        note = this.info.note?.decrypted,
        invoice = this.info.invoice?.decrypted,
        amountInBtc = this.info.amountInBtc.decrypted,
        amountInUsd = this.primal?.amountInUsd?.decrypted,
        exchangeRate = this.primal?.exchangeRate?.decrypted,
        totalFeeInBtc = this.info.totalFeeInBtc?.decrypted,
        otherUserId = this.info.otherUserId?.decrypted,
        otherLightningAddress = this.primal?.otherLightningAddress?.decrypted,
        otherUserProfile = otherProfile,
    )
