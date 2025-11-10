package net.primal.data.account.repository.processor

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.mappers.buildSessionEventData

class SessionEventProcessor(
    private val accountDatabase: AccountDatabase,
) {
    @OptIn(ExperimentalTime::class)
    suspend fun processAndPersist(
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
