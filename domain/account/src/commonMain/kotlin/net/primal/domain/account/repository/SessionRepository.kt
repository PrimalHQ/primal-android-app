package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppSession

interface SessionRepository {
    fun observeActiveSessions(signerPubKey: String): Flow<List<AppSession>>

    fun observeOngoingSessions(signerPubKey: String): Flow<List<AppSession>>

    fun observeActiveSessionForConnection(clientPubKey: String): Flow<AppSession?>

    fun observeSessionsByClientPubKey(clientPubKey: String): Flow<List<AppSession>>

    fun observeSession(sessionId: String): Flow<AppSession?>

    suspend fun getSession(sessionId: String): Result<AppSession>

    suspend fun findActiveSessionForConnection(clientPubKey: String): Result<AppSession>

    suspend fun startSession(clientPubKey: String): Result<String>

    suspend fun startSessionForClient(clientPubKey: String): Result<String>

    suspend fun endSessions(sessionIds: List<String>): Result<Unit>

    suspend fun endAllActiveSessions()

    suspend fun incrementActiveRelayCount(sessionIds: List<String>)

    suspend fun decrementActiveRelayCountOrEnd(sessionIds: List<String>)

    suspend fun setActiveRelayCount(sessionId: String, activeRelayCount: Int)
}
