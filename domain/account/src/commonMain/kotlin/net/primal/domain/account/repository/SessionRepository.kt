package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppSession

interface SessionRepository {
    fun observeActiveSessions(signerPubKey: String): Flow<List<AppSession>>

    fun observeOngoingSessions(signerPubKey: String): Flow<List<AppSession>>

    fun observeActiveSessionForConnection(connectionId: String): Flow<AppSession?>

    fun observeSessionsByConnectionId(connectionId: String): Flow<List<AppSession>>

    fun observeSession(sessionId: String): Flow<AppSession?>

    suspend fun startSession(connectionId: String): Result<String>

    suspend fun endSession(sessionId: String)

    suspend fun endAllActiveSessions()

    suspend fun incrementActiveRelayCount(sessionIds: List<String>)

    suspend fun decrementActiveRelayCountOrEnd(sessionIds: List<String>)

    suspend fun setActiveRelayCount(sessionId: String, activeRelayCount: Int)
}
