package net.primal.domain.transactions

import net.primal.domain.nostr.NostrEntity
import net.primal.domain.profile.ProfileData
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.WalletType

sealed class Transaction(
    open val transactionId: String,
    open val walletId: String,
    open val walletType: WalletType,
    open val type: TxType,
    open val state: TxState,
    open val createdAt: Long,
    open val updatedAt: Long,
    open val completedAt: Long?,
    open val userId: String,
    open val note: String?,
    open val invoice: String?,
    open val amountInBtc: Double,
    open val amountInUsd: Double?,
    open val exchangeRate: String?,
    open val totalFeeInBtc: String?,
) {
    /**
     * Lightning network payment.
     */
    data class Lightning(
        override val transactionId: String,
        override val walletId: String,
        override val walletType: WalletType,
        override val type: TxType,
        override val state: TxState,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val completedAt: Long?,
        override val userId: String,
        override val note: String?,
        override val invoice: String?,
        override val amountInBtc: Double,
        override val amountInUsd: Double?,
        override val exchangeRate: String?,
        override val totalFeeInBtc: String?,
        val otherUserId: String?,
        val otherLightningAddress: String?,
        val otherUserProfile: ProfileData?,
        val preimage: String?,
        val paymentHash: String?,
    ) : Transaction(
        transactionId = transactionId,
        walletId = walletId,
        walletType = walletType,
        type = type,
        state = state,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        userId = userId,
        note = note,
        invoice = invoice,
        amountInBtc = amountInBtc,
        amountInUsd = amountInUsd,
        exchangeRate = exchangeRate,
        totalFeeInBtc = totalFeeInBtc,
    )

    /**
     * Primal store purchase (premium subscription, legend contribution, etc.)
     */
    data class StorePurchase(
        override val transactionId: String,
        override val walletId: String,
        override val walletType: WalletType,
        override val type: TxType,
        override val state: TxState,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val completedAt: Long?,
        override val userId: String,
        override val note: String?,
        override val invoice: String?,
        override val amountInBtc: Double,
        override val amountInUsd: Double?,
        override val exchangeRate: String?,
        override val totalFeeInBtc: String?,
    ) : Transaction(
        transactionId = transactionId,
        walletId = walletId,
        walletType = walletType,
        type = type,
        state = state,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        userId = userId,
        note = note,
        invoice = invoice,
        amountInBtc = amountInBtc,
        amountInUsd = amountInUsd,
        exchangeRate = exchangeRate,
        totalFeeInBtc = totalFeeInBtc,
    )

    /**
     * Nostr zap (Lightning payment with attached nostr context).
     */
    data class Zap(
        override val transactionId: String,
        override val walletId: String,
        override val walletType: WalletType,
        override val type: TxType,
        override val state: TxState,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val completedAt: Long?,
        override val userId: String,
        override val note: String?,
        override val invoice: String?,
        override val amountInBtc: Double,
        override val amountInUsd: Double?,
        override val exchangeRate: String?,
        override val totalFeeInBtc: String?,
        val zappedEntity: NostrEntity,
        val otherUserId: String?,
        val otherLightningAddress: String?,
        val zappedByUserId: String?,
        val otherUserProfile: ProfileData?,
        val preimage: String?,
        val paymentHash: String?,
    ) : Transaction(
        transactionId = transactionId,
        walletId = walletId,
        walletType = walletType,
        type = type,
        state = state,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        userId = userId,
        note = note,
        invoice = invoice,
        amountInBtc = amountInBtc,
        amountInUsd = amountInUsd,
        exchangeRate = exchangeRate,
        totalFeeInBtc = totalFeeInBtc,
    )

    /**
     * Bitcoin on-chain transaction.
     */
    data class OnChain(
        override val transactionId: String,
        override val walletId: String,
        override val walletType: WalletType,
        override val type: TxType,
        override val state: TxState,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val completedAt: Long?,
        override val userId: String,
        override val note: String?,
        override val invoice: String?,
        override val amountInBtc: Double,
        override val amountInUsd: Double?,
        override val exchangeRate: String?,
        override val totalFeeInBtc: String?,
        val onChainTxId: String?,
        val onChainAddress: String?,
    ) : Transaction(
        transactionId = transactionId,
        walletId = walletId,
        walletType = walletType,
        type = type,
        state = state,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        userId = userId,
        note = note,
        invoice = invoice,
        amountInBtc = amountInBtc,
        amountInUsd = amountInUsd,
        exchangeRate = exchangeRate,
        totalFeeInBtc = totalFeeInBtc,
    )

    /**
     * Spark-to-Spark transfer (internal Spark network transfer).
     */
    data class Spark(
        override val transactionId: String,
        override val walletId: String,
        override val walletType: WalletType,
        override val type: TxType,
        override val state: TxState,
        override val createdAt: Long,
        override val updatedAt: Long,
        override val completedAt: Long?,
        override val userId: String,
        override val note: String?,
        override val invoice: String?,
        override val amountInBtc: Double,
        override val amountInUsd: Double?,
        override val exchangeRate: String?,
        override val totalFeeInBtc: String?,
        val sparkAddress: String?,
        val preimage: String?,
        val paymentHash: String?,
    ) : Transaction(
        transactionId = transactionId,
        walletId = walletId,
        walletType = walletType,
        type = type,
        state = state,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        userId = userId,
        note = note,
        invoice = invoice,
        amountInBtc = amountInBtc,
        amountInUsd = amountInUsd,
        exchangeRate = exchangeRate,
        totalFeeInBtc = totalFeeInBtc,
    )
}
