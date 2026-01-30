package net.primal.wallet.data.repository.mappers.local

import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nprofile
import net.primal.domain.nostr.toNostrString
import net.primal.domain.nostr.utils.asNostrEntity
import net.primal.domain.profile.ProfileData
import net.primal.domain.transactions.Transaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.TxKind
import net.primal.wallet.data.local.dao.WalletTransactionData

/**
 * Converts a persistence object to a domain transaction.
 */
internal fun WalletTransactionData.toDomain(otherProfile: ProfileData? = null): Transaction =
    when (this.txKind) {
        TxKind.ZAP -> toZapTransaction(otherProfile)
        TxKind.STORE_PURCHASE -> toStorePurchaseTransaction()
        TxKind.ON_CHAIN -> toOnChainTransaction()
        TxKind.LIGHTNING -> toLightningTransaction(otherProfile)
        TxKind.SPARK -> toSparkTransaction()
    }

private fun WalletTransactionData.toZapTransaction(otherProfile: ProfileData?): Transaction {
    val zappedEntity = this.zappedEntity?.decrypted?.asNostrEntity()?.getOrNull()
    return when (zappedEntity) {
        is Naddr, is Nevent, is Nprofile -> Transaction.Zap(
            transactionId = this.transactionId,
            walletId = this.walletId,
            walletType = this.walletType,
            type = this.type,
            state = this.state,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            completedAt = this.completedAt?.decrypted,
            userId = this.userId,
            note = this.note?.decrypted,
            invoice = this.invoice?.decrypted,
            amountInBtc = this.amountInBtc.decrypted,
            amountInUsd = this.amountInUsd?.decrypted,
            exchangeRate = this.exchangeRate?.decrypted,
            totalFeeInBtc = this.totalFeeInBtc?.decrypted,
            otherUserId = this.otherUserId?.decrypted,
            otherLightningAddress = this.otherLightningAddress?.decrypted,
            zappedEntity = zappedEntity,
            zappedByUserId = this.zappedByUserId?.decrypted,
            otherUserProfile = otherProfile,
            preimage = this.preimage,
            paymentHash = this.paymentHash,
        )

        else -> toLightningTransaction(otherProfile)
    }
}

private fun WalletTransactionData.toStorePurchaseTransaction() =
    Transaction.StorePurchase(
        transactionId = this.transactionId,
        walletId = this.walletId,
        walletType = this.walletType,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt?.decrypted,
        userId = this.userId,
        note = this.note?.decrypted,
        invoice = this.invoice?.decrypted,
        amountInBtc = this.amountInBtc.decrypted,
        amountInUsd = this.amountInUsd?.decrypted,
        exchangeRate = this.exchangeRate?.decrypted,
        totalFeeInBtc = this.totalFeeInBtc?.decrypted,
    )

private fun WalletTransactionData.toOnChainTransaction() =
    Transaction.OnChain(
        transactionId = this.transactionId,
        walletId = this.walletId,
        walletType = this.walletType,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt?.decrypted,
        userId = this.userId,
        note = this.note?.decrypted,
        invoice = this.invoice?.decrypted,
        amountInBtc = this.amountInBtc.decrypted,
        amountInUsd = this.amountInUsd?.decrypted,
        exchangeRate = this.exchangeRate?.decrypted,
        totalFeeInBtc = this.totalFeeInBtc?.decrypted,
        onChainTxId = this.onChainTxId?.decrypted,
        onChainAddress = this.onChainAddress?.decrypted,
    )

private fun WalletTransactionData.toLightningTransaction(otherProfile: ProfileData?) =
    Transaction.Lightning(
        transactionId = this.transactionId,
        walletId = this.walletId,
        walletType = this.walletType,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt?.decrypted,
        userId = this.userId,
        note = this.note?.decrypted,
        invoice = this.invoice?.decrypted,
        amountInBtc = this.amountInBtc.decrypted,
        amountInUsd = this.amountInUsd?.decrypted,
        exchangeRate = this.exchangeRate?.decrypted,
        totalFeeInBtc = this.totalFeeInBtc?.decrypted,
        otherUserId = this.otherUserId?.decrypted,
        otherLightningAddress = this.otherLightningAddress?.decrypted,
        otherUserProfile = otherProfile,
        preimage = this.preimage,
        paymentHash = this.paymentHash,
    )

private fun WalletTransactionData.toSparkTransaction() =
    Transaction.Spark(
        transactionId = this.transactionId,
        walletId = this.walletId,
        walletType = this.walletType,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt?.decrypted,
        userId = this.userId,
        note = this.note?.decrypted,
        invoice = this.invoice?.decrypted,
        amountInBtc = this.amountInBtc.decrypted,
        amountInUsd = this.amountInUsd?.decrypted,
        exchangeRate = this.exchangeRate?.decrypted,
        totalFeeInBtc = this.totalFeeInBtc?.decrypted,
        sparkAddress = null,
        preimage = this.preimage,
        paymentHash = this.paymentHash,
    )

/**
 * Converts a domain transaction to a persistence entity.
 */
internal fun Transaction.toWalletTransactionData(): WalletTransactionData =
    WalletTransactionData(
        transactionId = this.transactionId,
        walletId = this.walletId,
        walletType = this.walletType,
        type = this.type,
        state = this.state,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt?.asEncryptable(),
        amountInBtc = this.amountInBtc.asEncryptable(),
        userId = this.userId,
        note = this.note?.asEncryptable(),
        invoice = this.invoice?.asEncryptable(),
        totalFeeInBtc = this.totalFeeInBtc?.asEncryptable(),
        zappedEntity = extractZappedEntity()?.asEncryptable(),
        zappedByUserId = extractZappedByUserId()?.asEncryptable(),
        otherUserId = extractOtherUserId()?.asEncryptable(),
        txKind = resolveTxKind(),
        onChainAddress = extractOnChainAddress()?.asEncryptable(),
        onChainTxId = extractOnChainTxId()?.asEncryptable(),
        preimage = extractPreimage(),
        paymentHash = extractPaymentHash(),
        amountInUsd = extractAmountInUsd()?.asEncryptable(),
        exchangeRate = extractExchangeRate()?.asEncryptable(),
        otherLightningAddress = extractOtherLightningAddress()?.asEncryptable(),
    )

private fun Transaction.resolveTxKind(): TxKind =
    when (this) {
        is Transaction.Lightning -> TxKind.LIGHTNING
        is Transaction.StorePurchase -> TxKind.STORE_PURCHASE
        is Transaction.Zap -> TxKind.ZAP
        is Transaction.OnChain -> TxKind.ON_CHAIN
        is Transaction.Spark -> TxKind.SPARK
    }

private fun Transaction.extractZappedEntity(): String? =
    when (this) {
        is Transaction.Zap -> this.zappedEntity.toNostrString()
        else -> null
    }

private fun Transaction.extractZappedByUserId(): String? =
    when (this) {
        is Transaction.Zap -> this.zappedByUserId
        else -> null
    }

private fun Transaction.extractOtherUserId(): String? =
    when (this) {
        is Transaction.Lightning -> this.otherUserId
        is Transaction.Zap -> this.otherUserId
        else -> null
    }

private fun Transaction.extractOnChainAddress(): String? =
    when (this) {
        is Transaction.OnChain -> this.onChainAddress
        else -> null
    }

private fun Transaction.extractOnChainTxId(): String? =
    when (this) {
        is Transaction.OnChain -> this.onChainTxId
        else -> null
    }

fun Transaction.extractPreimage(): String? =
    when (this) {
        is Transaction.Lightning -> this.preimage
        is Transaction.Zap -> this.preimage
        is Transaction.Spark -> this.preimage
        else -> null
    }

fun Transaction.extractPaymentHash(): String? =
    when (this) {
        is Transaction.Lightning -> this.paymentHash
        is Transaction.Zap -> this.paymentHash
        is Transaction.Spark -> this.paymentHash
        else -> null
    }

private fun Transaction.extractAmountInUsd(): Double? =
    when (this) {
        is Transaction.Lightning -> this.amountInUsd
        is Transaction.Zap -> this.amountInUsd
        is Transaction.OnChain -> this.amountInUsd
        is Transaction.StorePurchase -> this.amountInUsd
        is Transaction.Spark -> this.amountInUsd
    }

private fun Transaction.extractExchangeRate(): String? =
    when (this) {
        is Transaction.Lightning -> this.exchangeRate
        is Transaction.Zap -> this.exchangeRate
        is Transaction.OnChain -> this.exchangeRate
        is Transaction.StorePurchase -> this.exchangeRate
        is Transaction.Spark -> this.exchangeRate
    }

private fun Transaction.extractOtherLightningAddress(): String? =
    when (this) {
        is Transaction.Lightning -> this.otherLightningAddress
        is Transaction.Zap -> this.otherLightningAddress
        else -> null
    }
