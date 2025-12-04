package net.primal.android.nostrconnect.handler

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.core.utils.onSuccess
import net.primal.domain.account.repository.SessionRepository

@Singleton
class RemoteSignerSessionHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
) {

    suspend fun startSession(connectionId: String) {
        sessionRepository.startSession(connectionId = connectionId)
            .onSuccess { PrimalRemoteSignerService.ensureServiceStarted(context = context) }
    }

    suspend fun startSessionForClient(clientPubKey: String) {
        sessionRepository.startSessionForClient(clientPubKey = clientPubKey)
            .onSuccess { PrimalRemoteSignerService.ensureServiceStarted(context = context) }
    }

    suspend fun endSession(sessionId: String) {
        sessionRepository.endSession(sessionId = sessionId)
    }
}
