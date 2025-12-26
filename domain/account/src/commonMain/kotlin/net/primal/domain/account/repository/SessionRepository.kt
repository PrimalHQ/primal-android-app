package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.model.RemoteAppSession

interface SessionRepository {

    fun observeOngoingSessions(signerPubKey: String): Flow<List<RemoteAppSession>>

    fun observeOngoingSessionForConnection(appIdentifier: String): Flow<RemoteAppSession?>

    fun observeSessionsByApp(appIdentifier: String): Flow<List<AppSession>>

    fun observeRemoteSession(sessionId: String): Flow<RemoteAppSession?>

    fun observeLocalSession(sessionId: String): Flow<AppSession?>

    suspend fun getSession(sessionId: String): Result<RemoteAppSession>

    suspend fun findFirstOpenSessionByAppIdentifier(appIdentifier: String): Result<RemoteAppSession>

    suspend fun startRemoteSession(appIdentifier: String): Result<String>

    suspend fun endSessions(sessionIds: List<String>): Result<Unit>

    suspend fun endAllActiveSessions()
}
