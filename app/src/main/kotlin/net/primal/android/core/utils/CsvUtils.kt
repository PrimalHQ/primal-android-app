package net.primal.android.core.utils

import android.content.Context
import android.net.Uri
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.Writer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.domain.nostr.toNostrString
import net.primal.domain.profile.ProfileData
import net.primal.domain.transactions.Transaction

private val CSV_HEADERS = listOf(
    "transactionId", "walletId", "type", "state", "createdAt", "updatedAt", "completedAt", "userId",
    "note", "invoice", "amountInBtc", "amountInUsd", "exchangeRate", "totalFeeInBtc",
    "otherUserId", "otherLightningAddress", "otherUserProfile", "preimage", "paymentHash",
    "zappedEntity", "zappedByUserId", "onChainTxId", "onChainAddress", "sparkAddress",
)

suspend fun saveTransactionsToUri(
    context: Context,
    uri: Uri,
    transactions: List<Transaction>,
) = runCatching {
    withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use {
                outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                writeCsv(writer, transactions)
            }
        }
    }
}

private fun writeCsv(writer: Writer, transactions: List<Transaction>) {
    writer.write(CSV_HEADERS.joinToString(",") { it.escapeCsv() })
    writer.write(System.lineSeparator())
    transactions.forEach { transaction ->
        writer.write(transaction.toCsvLine())
        writer.write(System.lineSeparator())
    }
}

private fun Transaction.toCsvLine(): String {
    val data = mutableMapOf<String, Any?>()

    data["transactionId"] = transactionId
    data["walletId"] = walletId
    data["type"] = type
    data["state"] = state
    data["createdAt"] = createdAt
    data["updatedAt"] = updatedAt
    data["completedAt"] = completedAt
    data["userId"] = userId
    data["note"] = note
    data["invoice"] = invoice
    data["amountInBtc"] = amountInBtc
    data["amountInUsd"] = amountInUsd
    data["exchangeRate"] = exchangeRate
    data["totalFeeInBtc"] = totalFeeInBtc

    when (this) {
        is Transaction.Lightning -> {
            data["otherUserId"] = otherUserId
            data["otherLightningAddress"] = otherLightningAddress
            data["otherUserProfile"] = otherUserProfile?.formatForCsv()
            data["preimage"] = preimage
            data["paymentHash"] = paymentHash
        }
        is Transaction.Zap -> {
            data["otherUserId"] = otherUserId
            data["otherLightningAddress"] = otherLightningAddress
            data["otherUserProfile"] = otherUserProfile?.formatForCsv()
            data["preimage"] = preimage
            data["paymentHash"] = paymentHash
            data["zappedEntity"] = zappedEntity.toNostrString()
            data["zappedByUserId"] = zappedByUserId
        }
        is Transaction.OnChain -> {
            data["onChainTxId"] = onChainTxId
            data["onChainAddress"] = onChainAddress
        }
        is Transaction.Spark -> {
            data["sparkAddress"] = sparkAddress
            data["preimage"] = preimage
            data["paymentHash"] = paymentHash
        }
        is Transaction.StorePurchase -> Unit
    }

    return CSV_HEADERS.joinToString(",") { header ->
        data[header]?.toString()?.escapeCsv() ?: ""
    }
}

private fun ProfileData.formatForCsv(): String {
    return displayName ?: handle ?: profileId
}

private fun String.escapeCsv(): String {
    return if (contains(",") || contains("\"") || contains("\n")) {
        "\"" + replace("\"", "\"\"") + "\""
    } else {
        this
    }
}
