package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.model.LocalAppSession
import net.primal.domain.account.model.RemoteAppSession

interface SessionRepository {
    fun observeActiveSessions(signerPubKey: String): Flow<List<RemoteAppSession>>

    fun observeOngoingSessions(signerPubKey: String): Flow<List<RemoteAppSession>>

    fun observeActiveSessionForConnection(clientPubKey: String): Flow<RemoteAppSession?>

    fun observeOngoingSessionForConnection(clientPubKey: String): Flow<RemoteAppSession?>

    fun observeSessionsByClientPubKey(clientPubKey: String): Flow<List<RemoteAppSession>>

    fun observeSessionsByAppIdentifier(appIdentifier: String): Flow<List<AppSession>>

    fun observeSession(sessionId: String): Flow<RemoteAppSession?>

    fun observeRemoteSession(sessionId: String): Flow<RemoteAppSession?>

    fun observeLocalSession(sessionId: String): Flow<LocalAppSession?>

    suspend fun getSession(sessionId: String): Result<RemoteAppSession>

    suspend fun findActiveSessionForConnection(clientPubKey: String): Result<RemoteAppSession>

    suspend fun startSession(clientPubKey: String): Result<String>

    suspend fun endSessions(sessionIds: List<String>): Result<Unit>

    suspend fun endAllActiveSessions()
}
