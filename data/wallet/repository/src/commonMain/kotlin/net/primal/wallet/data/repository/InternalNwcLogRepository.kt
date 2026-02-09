package net.primal.wallet.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching
import net.primal.domain.wallet.nwc.model.NwcRequestState
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.nwc.mapper.buildNwcRequestLog

internal class InternalNwcLogRepository(
    private val walletDatabase: WalletDatabase,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun logRequest(request: WalletNwcRequest, requestedAt: Long) =
        withContext(dispatchers.io()) {
            runCatching {
                walletDatabase.nwcLogs().upsert(
                    buildNwcRequestLog(
                        request = request,
                        requestedAt = requestedAt,
                    ),
                )
            }.onFailure { Napier.e(it) { "Failed to log NWC request." } }
        }

    suspend fun updateLogWithResponse(
        eventId: String,
        responsePayload: String,
        requestState: NwcRequestState,
        completedAt: Long,
        errorCode: String? = null,
        errorMessage: String? = null,
    ) = withContext(dispatchers.io()) {
        runCatching {
            walletDatabase.nwcLogs().updateResponse(
                eventId = eventId,
                responsePayload = responsePayload.asEncryptable(),
                requestState = requestState.name.asEncryptable(),
                completedAt = completedAt,
                errorCode = errorCode?.asEncryptable(),
                errorMessage = errorMessage?.asEncryptable(),
            )
        }.onFailure { Napier.e(it) { "Failed to update NWC log response." } }
    }
}
