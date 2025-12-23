package net.primal.data.account.repository.repository

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.mapCatching
import net.primal.core.utils.runCatching
import net.primal.data.account.local.dao.apps.AppSessionData
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asDomain
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.repository.SessionRepository
import net.primal.shared.data.local.db.withTransaction

@OptIn(ExperimentalTime::class)
class SessionRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) : SessionRepository {

    override fun observeActiveSessions(signerPubKey: String): Flow<List<AppSession>> =
        database.remoteAppSessions().observeActiveSessions(signerPubKey = signerPubKey)
            .map { list -> list.map { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeOngoingSessions(signerPubKey: String): Flow<List<AppSession>> =
        database.remoteAppSessions().observeOngoingSessions(signerPubKey = signerPubKey)
            .map { list -> list.map { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeActiveSessionForConnection(clientPubKey: String): Flow<AppSession?> =
        database.remoteAppSessions().observeActiveSessionForConnection(clientPubKey)
            .map { it?.asDomain() }
            .distinctUntilChanged()

    override fun observeOngoingSessionForConnection(clientPubKey: String): Flow<AppSession?> =
        database.remoteAppSessions().observeOngoingSessionForConnection(clientPubKey)
            .map { it?.asDomain() }
            .distinctUntilChanged()

    override fun observeSessionsByClientPubKey(clientPubKey: String): Flow<List<AppSession>> =
        database.remoteAppSessions().observeSessionsByClientPubKey(clientPubKey)
            .map { list -> list.map { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeSession(sessionId: String): Flow<AppSession?> =
        database.remoteAppSessions().observeSession(sessionId = sessionId)
            .map { it?.asDomain() }
            .distinctUntilChanged()

    override suspend fun getSession(sessionId: String): Result<AppSession> =
        withContext(dispatchers.io()) {
            database.remoteAppSessions().findSession(sessionId = sessionId)
                ?.asDomain()?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't find session with id $sessionId."))
        }

    override suspend fun findActiveSessionForConnection(clientPubKey: String): Result<AppSession> =
        withContext(dispatchers.io()) {
            runCatching {
                database.remoteAppSessions().findActiveSessionByClientPubKey(appIdentifier = clientPubKey)?.asDomain()
                    ?: throw NoSuchElementException("Couldn't find active session for connection $clientPubKey.")
            }
        }

    override suspend fun startSession(clientPubKey: String): Result<String> =
        withContext(dispatchers.io()) {
            Napier.d(tag = "Signer") { "Starting session for $clientPubKey" }
            val existingSession = database.remoteAppSessions().findAnyOpenSessionByAppIdentifier(
                appIdentifier = clientPubKey,
            )
            if (existingSession == null) {
                val newSession = AppSessionData(appIdentifier = clientPubKey)
                database.appSessions().upsertAll(data = listOf(newSession))
                Napier.d(tag = "Signer") { "Successfully started session." }
                newSession.sessionId.asSuccess()
            } else {
                Napier.d(tag = "Signer") { "Starting session failed (session already exists)." }
                Result.failure(
                    IllegalStateException("There is an already active session for connection $clientPubKey."),
                )
            }
        }

    override suspend fun startSessionForClient(clientPubKey: String): Result<String> =
        withContext(dispatchers.io()) {
            runCatching {
                database.remoteAppConnections().getConnection(clientPubKey = clientPubKey)
                    ?: throw NoSuchElementException("Couldn't find connection for $clientPubKey")
            }.mapCatching {
                startSession(clientPubKey = it.data.clientPubKey).getOrThrow()
            }
        }

    override suspend fun endSessions(sessionIds: List<String>) =
        withContext(dispatchers.io()) {
            runCatching {
                val now = Clock.System.now().epochSeconds
                database.withTransaction {
                    sessionIds.forEach { sessionId ->
                        database.appSessions().endSession(sessionId = sessionId, endedAt = now)
                    }
                }
            }
        }

    override suspend fun endAllActiveSessions() =
        withContext(dispatchers.io()) {
            database.appSessions().endAllActiveSessions(endedAt = Clock.System.now().epochSeconds)
        }

    override suspend fun incrementActiveRelayCount(sessionIds: List<String>) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                sessionIds.forEach {
                    database.appSessions().incrementActiveRelayCount(sessionId = it)
                }
            }
        }

    override suspend fun decrementActiveRelayCountOrEnd(sessionIds: List<String>) =
        withContext(dispatchers.io()) {
            database.withTransaction {
                sessionIds.forEach {
                    database.appSessions().decrementActiveRelayCountOrEnd(sessionId = it)
                }
            }
        }

    override suspend fun setActiveRelayCount(sessionId: String, activeRelayCount: Int) =
        withContext(dispatchers.io()) {
            database.appSessions().setActiveRelayCount(sessionId = sessionId, activeRelayCount = activeRelayCount)
        }
}
