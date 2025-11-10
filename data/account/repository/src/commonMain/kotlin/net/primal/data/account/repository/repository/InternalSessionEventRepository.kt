package net.primal.data.account.repository.repository

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.mappers.buildSessionEventData

@OptIn(ExperimentalTime::class)
internal class InternalSessionEventRepository(
    private val accountDatabase: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun saveSessionEvent(
        sessionId: String,
        requestedAt: Long,
        method: RemoteSignerMethod,
        response: RemoteSignerMethodResponse,
    ) {
        val completedAt = Clock.System.now().epochSeconds

        buildSessionEventData(
            sessionId = sessionId,
            requestedAt = requestedAt,
            completedAt = completedAt,
            method = method,
            response = response,
        )?.let { sessionEventData ->
            accountDatabase.sessionEvents().upsert(data = sessionEventData)
        }
    }
}
