package net.primal.android.nostrconnect.handler

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.domain.account.repository.SessionRepository

@Singleton
class RemoteSignerSessionHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
) {

    suspend fun startSession(clientPubKey: String) = runCatching {
        PrimalRemoteSignerService.ensureServiceStarted(context = context)
        sessionRepository.startSession(clientPubKey = clientPubKey)
    }

    suspend fun endSessions(sessionIds: List<String>) = sessionRepository.endSessions(sessionIds = sessionIds)
}
