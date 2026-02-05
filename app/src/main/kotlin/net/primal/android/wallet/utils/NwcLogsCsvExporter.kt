package net.primal.android.wallet.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.encodeToString
import net.primal.domain.wallet.nwc.model.NwcRequestLog

@Serializable
private data class NwcLogCsvRecord(
    val eventId: String,
    val connectionId: String,
    val walletId: String,
    val userId: String,
    val appName: String,
    val method: String,
    val requestedAt: Long,
    val completedAt: Long?,
    val status: String,
    val errorCode: String?,
    val errorMessage: String?,
    val amountMsats: Long?,
    val requestPayload: String,
    val responsePayload: String?,
)

@OptIn(ExperimentalSerializationApi::class)
suspend fun saveNwcLogsToUri(
    context: Context,
    uri: Uri,
    logs: List<NwcRequestLog>,
) = runCatching {
    withContext(Dispatchers.IO) {
        val csv = Csv {
            hasHeaderRecord = true
            recordSeparator = System.lineSeparator()
        }
        val records = logs.map { it.toCsvRecord() }
        val csvContent = csv.encodeToString(records)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(csvContent.toByteArray())
        }
    }
}

private fun NwcRequestLog.toCsvRecord(): NwcLogCsvRecord {
    return NwcLogCsvRecord(
        eventId = eventId,
        connectionId = connectionId,
        walletId = walletId,
        userId = userId,
        appName = appName,
        method = method,
        requestedAt = requestedAt,
        completedAt = completedAt,
        status = requestState.name,
        errorCode = errorCode,
        errorMessage = errorMessage,
        amountMsats = amountMsats,
        requestPayload = requestPayload,
        responsePayload = responsePayload,
    )
}
