package net.primal.data.account.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asDomain
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.repository.SessionEventRepository

class SessionEventRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) : SessionEventRepository {
    override fun getEventsForSession(sessionId: String): Flow<List<SessionEvent>> {
        return database.sessionEvents().observeEventsBySessionId(sessionId = sessionId)
            .map { list -> list.mapNotNull { it.asDomain() } }
            .distinctUntilChanged()
    }
}
