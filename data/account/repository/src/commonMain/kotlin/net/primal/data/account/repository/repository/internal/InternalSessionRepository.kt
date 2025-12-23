package net.primal.data.account.repository.repository.internal

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.shared.data.local.db.withTransaction

class InternalSessionRepository(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) {

    suspend fun incrementActiveRelayCount(sessionIds: List<String>) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                sessionIds.forEach {
                    database.appSessions().incrementActiveRelayCount(sessionId = it)
                }
            }
        }

    suspend fun decrementActiveRelayCountOrEnd(sessionIds: List<String>) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                sessionIds.forEach {
                    database.appSessions().decrementActiveRelayCountOrEnd(sessionId = it)
                }
            }
        }

    suspend fun setActiveRelayCount(sessionId: String, activeRelayCount: Int) =
        withContext(dispatchers.io()) {
            database.appSessions().setActiveRelayCount(sessionId = sessionId, activeRelayCount = activeRelayCount)
        }
}
