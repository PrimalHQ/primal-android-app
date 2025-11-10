package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.domain.account.model.SessionEvent

interface SessionEventRepository {
    fun getEventsForSession(sessionId: String): Flow<List<SessionEvent>>
}
