package net.primal.data.account.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asDomain
import net.primal.domain.account.model.SessionLog
import net.primal.domain.account.repository.SessionLogRepository

class SessionLogRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) : SessionLogRepository {
    override fun getLogsForSession(sessionId: String): Flow<List<SessionLog>> {
        return database.sessionLogs().observeLogsBySessionId(sessionId = sessionId)
            .map { list -> list.map { it.asDomain() } }
    }
}
