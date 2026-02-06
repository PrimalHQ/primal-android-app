package net.primal.android.wallet.utils

import android.content.Context
import android.net.Uri
import kotlinx.serialization.Serializable
import net.primal.android.core.utils.saveCsvToUri
import net.primal.domain.nostr.toNostrString
import net.primal.domain.transactions.Transaction

@Serializable
private data class TransactionCsvRecord(
    val transactionId: String,
    val walletId: String,
    val type: String,
    val state: String,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val userId: String,
    val note: String?,
    val invoice: String?,
    val amountInBtc: Double,
    val amountInUsd: Double?,
    val exchangeRate: String?,
    val totalFeeInBtc: String?,
    val otherUserId: String?,
    val otherLightningAddress: String?,
    val otherUserProfile: String?,
    val preimage: String?,
    val paymentHash: String?,
    val zappedEntity: String?,
    val zappedByUserId: String?,
    val onChainTxId: String?,
    val onChainAddress: String?,
    val sparkAddress: String?,
)

suspend fun saveTransactionsToUri(
    context: Context,
    uri: Uri,
    transactions: List<Transaction>,
) = runCatching {
    val records = transactions.map { it.toCsvRecord() }
    saveCsvToUri(context, uri, records)
}

private fun Transaction.toCsvRecord(): TransactionCsvRecord {
    val baseRecord = TransactionCsvRecord(
        transactionId = transactionId,
        walletId = walletId,
        type = type.name,
        state = state.name,
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
        otherUserId = null,
        otherLightningAddress = null,
        otherUserProfile = null,
        preimage = null,
        paymentHash = null,
        zappedEntity = null,
        zappedByUserId = null,
        onChainTxId = null,
        onChainAddress = null,
        sparkAddress = null,
    )

    return when (this) {
        is Transaction.Lightning -> baseRecord.copy(
            otherUserId = otherUserId,
            otherLightningAddress = otherLightningAddress,
            otherUserProfile = otherUserProfile?.let { it.displayName ?: it.handle ?: it.profileId },
            preimage = preimage,
            paymentHash = paymentHash,
        )
        is Transaction.Zap -> baseRecord.copy(
            otherUserId = otherUserId,
            otherLightningAddress = otherLightningAddress,
            otherUserProfile = otherUserProfile?.let { it.displayName ?: it.handle ?: it.profileId },
            preimage = preimage,
            paymentHash = paymentHash,
            zappedEntity = zappedEntity.toNostrString(),
            zappedByUserId = zappedByUserId,
        )
        is Transaction.OnChain -> baseRecord.copy(
            onChainTxId = onChainTxId,
            onChainAddress = onChainAddress,
        )
        is Transaction.Spark -> baseRecord.copy(
            sparkAddress = sparkAddress,
            preimage = preimage,
            paymentHash = paymentHash,
        )
        is Transaction.StorePurchase -> baseRecord
    }
}
