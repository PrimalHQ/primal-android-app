package net.primal.data.account.repository.repository.internal

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.dao.apps.AppSessionData
import net.primal.data.account.local.dao.apps.AppSessionType
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asDomain
import net.primal.domain.account.model.AppSession
import net.primal.shared.data.local.db.withTransaction

class InternalSessionRepository(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) {

    suspend fun getOrCreateLocalAppSession(appIdentifier: String): AppSession =
        withContext(dispatchers.io()) {
            val latestAppSession = database.appSessions().findLatestAppSession(appIdentifier = appIdentifier)
                ?: run {
                    val newSession = AppSessionData(
                        appIdentifier = appIdentifier,
                        sessionType = AppSessionType.LocalSession,
                    )
                    database.appSessions().upsertAll(data = listOf(newSession))
                    newSession
                }

            latestAppSession.asDomain()
        }

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
