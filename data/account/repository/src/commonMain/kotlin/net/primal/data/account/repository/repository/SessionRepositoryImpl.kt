package net.primal.data.account.repository.repository

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.asSuccess
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.account.local.dao.apps.AppSessionData
import net.primal.data.account.local.dao.apps.AppSessionType
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.repository.mappers.asDomain
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.model.RemoteAppSession
import net.primal.domain.account.repository.SessionRepository
import net.primal.shared.data.local.db.withTransaction

class SessionRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
) : SessionRepository {

    override fun observeOngoingSessions(signerPubKey: String): Flow<List<RemoteAppSession>> =
        database.remoteAppSessions().observeOngoingSessionsBySigner(signerPubKey = signerPubKey)
            .map { list -> list.map { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeOngoingSessionForConnection(clientPubKey: String): Flow<RemoteAppSession?> =
        database.remoteAppSessions().observeOngoingSessionByApp(clientPubKey)
            .map { it?.asDomain() }
            .distinctUntilChanged()

    override fun observeSessionsByApp(appIdentifier: String): Flow<List<AppSession>> =
        database.appSessions().observeSessionsByApp(appIdentifier = appIdentifier)
            .map { list -> list.map { it.asDomain() } }
            .distinctUntilChanged()

    override fun observeRemoteSession(sessionId: String): Flow<RemoteAppSession?> =
        database.remoteAppSessions().observeSession(sessionId = sessionId)
            .map { it?.asDomain() }
            .distinctUntilChanged()

    override fun observeLocalSession(sessionId: String): Flow<AppSession?> =
        database.appSessions().observeSession(sessionId = sessionId)
            .map { it?.asDomain() }
            .distinctUntilChanged()

    override suspend fun getSession(sessionId: String): Result<RemoteAppSession> =
        withContext(dispatchers.io()) {
            database.remoteAppSessions().findSession(sessionId = sessionId)
                ?.asDomain()?.asSuccess()
                ?: Result.failure(NoSuchElementException("Couldn't find session with id $sessionId."))
        }

    override suspend fun findFirstOpenSessionByAppIdentifier(appIdentifier: String): Result<RemoteAppSession> =
        withContext(dispatchers.io()) {
            runCatching {
                database.remoteAppSessions().findOngoingSessionByApp(appIdentifier = appIdentifier)?.asDomain()
                    ?: throw NoSuchElementException("Couldn't find open session for connection $appIdentifier.")
            }
        }

    override suspend fun startSession(clientPubKey: String): Result<String> =
        withContext(dispatchers.io()) {
            Napier.d(tag = "Signer") { "Starting session for $clientPubKey" }
            val existingOpenSession = database.remoteAppSessions().findOngoingSessionByApp(
                appIdentifier = clientPubKey,
            )
            if (existingOpenSession == null) {
                val newSession = AppSessionData(
                    appIdentifier = clientPubKey,
                    sessionType = AppSessionType.RemoteSession,
                )
                database.appSessions().upsertAll(data = listOf(newSession))
                Napier.d(tag = "Signer") { "Successfully started session." }
                newSession.sessionId.asSuccess()
            } else {
                Napier.d(tag = "Signer") { "Existing open session already exists for $clientPubKey. Start ignored." }
                Result.failure(
                    IllegalStateException("There is an existing open session for connection $clientPubKey."),
                )
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
            database.appSessions().endAllOngoingSessions(endedAt = Clock.System.now().epochSeconds)
        }
}
