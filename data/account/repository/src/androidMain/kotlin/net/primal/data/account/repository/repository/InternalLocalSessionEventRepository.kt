package net.primal.data.account.repository.repository

import kotlin.time.Clock
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.local.dao.apps.SignerMethodType
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.buildSessionEventData
import net.primal.data.account.repository.repository.internal.model.UpdateAppSessionEventRequest
import net.primal.data.account.signer.local.model.LocalSignerMethod
import net.primal.data.account.signer.local.model.LocalSignerMethodResponse
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable

internal class InternalLocalSessionEventRepository(
    private val accountDatabase: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) {

    suspend fun saveLocalSessionEvent(
        sessionId: String,
        requestType: SignerMethodType,
        method: LocalSignerMethod?,
        response: LocalSignerMethodResponse?,
        requestState: AppRequestState? = null,
    ) = withContext(dispatchers.io()) {
        runCatching {
            val completedAt = Clock.System.now().epochSeconds
            buildSessionEventData(
                sessionId = sessionId,
                requestedAt = method?.requestedAt ?: completedAt,
                completedAt = if (response != null) completedAt else null,
                requestType = requestType,
                method = method,
                response = response,
                requestState = requestState,
            )?.let { sessionEventData ->
                accountDatabase.localAppSessionEvents().insert(data = sessionEventData)
            } ?: throw IllegalArgumentException("Couldn't build session event data.")
        }
    }

    suspend fun updateLocalAppSessionEventState(requests: List<UpdateAppSessionEventRequest>) =
        withContext(dispatchers.io()) {
            accountDatabase.withTransaction {
                requests.forEach { request ->
                    accountDatabase.localAppSessionEvents().updateSessionEventRequestState(
                        eventId = request.eventId,
                        requestState = request.requestState,
                        responsePayload = request.responsePayload?.asEncryptable(),
                        completedAt = request.completedAt,
                    )
                }
            }
        }
}
