package net.primal.android.wallet.utils

import android.content.Context
import android.net.Uri
import java.time.Instant
import java.time.format.FormatStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.core.utils.formatToDefaultDateTimeFormat
import net.primal.android.core.utils.saveCsvToUri
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.domain.transactions.Transaction

@Serializable
private data class TransactionCsvRecord(
    @SerialName("Type") val type: String,
    @SerialName("Amount (sats)") val amountSats: Long,
    @SerialName("Fee (sats)") val feeSats: Long,
    @SerialName("State") val state: String,
    @SerialName("Date") val transactionDate: String,
    @SerialName("Note") val note: String,
    @SerialName("TransactionId") val transactionId: String,
    @SerialName("Invoice") val invoice: String,
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
    return TransactionCsvRecord(
        type = type.name,
        amountSats = amountInBtc.toSats().toLong(),
        feeSats = totalFeeInBtc?.toDoubleOrNull()?.toSats()?.toLong() ?: 0L,
        state = state.name,
        transactionDate = completedAt?.let {
            Instant.ofEpochSecond(it).formatToDefaultDateTimeFormat(FormatStyle.MEDIUM)
        } ?: "",
        note = note ?: "",
        transactionId = transactionId,
        invoice = invoice ?: "",
    )
}
