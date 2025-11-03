package net.primal.data.account.repository.repository

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.local.dao.AppSessionData
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asDomain
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.repository.SessionRepository
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable

@OptIn(ExperimentalTime::class)
class SessionRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) : SessionRepository {
    override fun observeActiveSessions(signerPubKey: String): Flow<List<AppSession>> =
        database.sessions().observeActiveSessions(signerPubKey = signerPubKey.asEncryptable())
            .map { list -> list.map { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeNonEndedSessions(signerPubKey: String): Flow<List<AppSession>> =
        database.sessions().observeNonEndedSessions(signerPubKey = signerPubKey.asEncryptable())
            .map { list -> list.map { it.asDomain() } }
            .distinctUntilChanged()

    override suspend fun startSession(connectionId: String): Result<String> =
        withContext(dispatchers.io()) {
            val existingSession = database.sessions().findActiveSessionByConnectionId(connectionId = connectionId)
            if (existingSession == null) {
                val newSession = AppSessionData(connectionId = connectionId)
                database.sessions().upsertAll(data = listOf(newSession))
                newSession.sessionId.asSuccess()
            } else {
                Result.failure(
                    IllegalStateException("There is an already active session for this connection."),
                )
            }
        }

    override suspend fun endSession(sessionId: String) =
        withContext(dispatchers.io()) {
            database.sessions().endSession(sessionId = sessionId, endedAt = Clock.System.now().epochSeconds)
        }

    override suspend fun endAllActiveSessions() =
        withContext(dispatchers.io()) {
            database.sessions().endAllActiveSessions(endedAt = Clock.System.now().epochSeconds)
        }

    override suspend fun incrementActiveRelayCount(sessionIds: List<String>) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                sessionIds.forEach {
                    database.sessions().incrementActiveRelayCount(sessionId = it)
                }
            }
        }

    override suspend fun decrementActiveRelayCountOrEnd(sessionIds: List<String>) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                sessionIds.forEach {
                    database.sessions().decrementActiveRelayCountOrEnd(sessionId = it)
                }
            }
        }
}
