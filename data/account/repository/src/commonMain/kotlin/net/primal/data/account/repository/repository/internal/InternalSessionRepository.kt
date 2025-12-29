package net.primal.data.account.repository.repository.internal

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
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
            database.withTransaction {
                val latestAppSession = database.appSessions().findLatestSessionByApp(appIdentifier = appIdentifier)
                val now = Clock.System.now().epochSeconds

                if (latestAppSession != null && latestAppSession.endedAt == null) {
                    val expired = isSessionExpired(
                        sessionId = latestAppSession.sessionId,
                        sessionStartedAt = latestAppSession.startedAt,
                        now = now,
                    )

                    if (!expired) {
                        return@withTransaction latestAppSession.asDomain()
                    }

                    database.appSessions().endSession(
                        sessionId = latestAppSession.sessionId,
                        endedAt = now,
                    )
                }

                val newSession = AppSessionData(
                    appIdentifier = appIdentifier,
                    sessionType = AppSessionType.LocalSession,
                    startedAt = now,
                )
                database.appSessions().upsertAll(data = listOf(newSession))
                newSession.asDomain()
            }
        }

    private suspend fun isSessionExpired(
        sessionId: String,
        sessionStartedAt: Long,
        now: Long,
    ): Boolean {
        val sessionTimeout = 15.minutes.inWholeSeconds

        val lastActivityAt = database.localAppSessionEvents()
            .getLastActivityAt(sessionId) ?: sessionStartedAt

        return (now - lastActivityAt) > sessionTimeout
    }
}
