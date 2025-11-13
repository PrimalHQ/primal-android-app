package net.primal.data.account.repository.repository

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.dao.RequestState
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.mappers.buildSessionEventData
import net.primal.data.account.repository.repository.model.UpdateSessionEventRequest
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable

@OptIn(ExperimentalTime::class)
internal class InternalSessionEventRepository(
    private val accountDatabase: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun saveSessionEvent(
        sessionId: String,
        signerPubKey: String,
        method: RemoteSignerMethod,
        response: RemoteSignerMethodResponse?,
    ) = withContext(dispatchers.io()) {
        val completedAt = Clock.System.now().epochSeconds

        buildSessionEventData(
            sessionId = sessionId,
            signerPubKey = signerPubKey,
            requestedAt = method.requestedAt,
            completedAt = completedAt,
            method = method,
            response = response,
        )?.let { sessionEventData ->
            accountDatabase.sessionEvents().upsert(data = sessionEventData)
        }
    }

    fun observePendingResponseEvents(signerPubKey: String) =
        accountDatabase.sessionEvents().observeEventsByRequestState(
            signerPubKey = signerPubKey.asEncryptable(),
            requestState = RequestState.PendingResponse,
        ).distinctUntilChanged()

    suspend fun updateSessionEventState(requests: List<UpdateSessionEventRequest>) =
        withContext(dispatchers.io()) {
            accountDatabase.withTransaction {
                requests.forEach { request ->
                    accountDatabase.sessionEvents().updateSessionEventRequestState(
                        eventId = request.eventId,
                        requestState = request.requestState,
                        responsePayload = request.responsePayload?.asEncryptable(),
                    )
                }
            }
        }
}
