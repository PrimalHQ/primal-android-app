package net.primal.android.nostrconnect.handler

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.core.utils.fold
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository

@Singleton
class RemoteSignerSessionHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
    private val connectionRepository: ConnectionRepository,
) {

    suspend fun startSession(clientPubKey: String) =
        runCatching {
            PrimalRemoteSignerService.ensureServiceStarted(context = context)
            sessionRepository.startRemoteSession(appIdentifier = clientPubKey)
        }

    suspend fun isAutoStartEnabled(clientPubKey: String) =
        connectionRepository.getConnectionByClientPubKey(clientPubKey = clientPubKey).fold(
            onSuccess = { it.autoStart },
            onFailure = { false },
        )

    suspend fun hasActiveSession(clientPubKey: String) =
        sessionRepository.findFirstOpenSessionByAppIdentifier(appIdentifier = clientPubKey).isSuccess

    suspend fun endSessions(sessionIds: List<String>) = sessionRepository.endSessions(sessionIds = sessionIds)
}
