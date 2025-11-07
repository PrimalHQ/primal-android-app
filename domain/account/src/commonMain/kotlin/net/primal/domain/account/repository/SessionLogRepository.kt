package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.domain.account.model.SessionLog

interface SessionLogRepository {
    fun getLogsForSession(sessionId: String): Flow<List<SessionLog>>
}
