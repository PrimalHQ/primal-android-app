package net.primal.android.wallet.utils

import android.content.Context
import android.net.Uri
import kotlinx.serialization.Serializable
import net.primal.android.core.utils.saveCsvToUri
import net.primal.domain.wallet.nwc.model.NwcRequestLog

@Serializable
private data class NwcLogCsvRecord(
    val eventId: String,
    val connectionId: String,
    val walletId: String,
    val userId: String,
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

suspend fun saveNwcLogsToUri(
    context: Context,
    uri: Uri,
    logs: List<NwcRequestLog>,
) = runCatching {
    val records = logs.map { it.toCsvRecord() }
    saveCsvToUri(context, uri, records)
}

private fun NwcRequestLog.toCsvRecord(): NwcLogCsvRecord {
    return NwcLogCsvRecord(
        eventId = eventId,
        connectionId = connectionId,
        walletId = walletId,
        userId = userId,
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
