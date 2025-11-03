package net.primal.data.account.repository.service

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.handler.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.data.account.repository.manager.model.RelayEvent
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrKeyPair

class RemoteSignerServiceImpl internal constructor(
    private val signerKeyPair: NostrKeyPair,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val nostrRelayManager: NostrRelayManager,
    private val remoteSignerMethodResponseBuilder: RemoteSignerMethodResponseBuilder,
) : RemoteSignerService {

    private val scope = CoroutineScope(SupervisorJob())

    private var relaySessionMap = emptyMap<String, List<String>>()
    private var activeClientPubKeys = HashSet<String>()

    override fun start() {
        Napier.d(tag = "Signer") { "RemoteSignerService started." }
        observeNonEndedSessions()
        observeRelayEvents()
        observeMethods()
        observeErrors()
    }

    private fun observeNonEndedSessions() =
        scope.launch {
            sessionRepository.observeNonEndedSessions(signerPubKey = signerKeyPair.pubKey)
                .collect { sessions ->
                    relaySessionMap = sessions
                        .flatMap { session ->
                            session.relays.map { relay -> relay to session.sessionId }
                        }
                        .groupBy(
                            keySelector = { it.first },
                            valueTransform = { it.second },
                        )

                    activeClientPubKeys = sessions.map { it.clientPubKey }.toHashSet()
                    nostrRelayManager.connectToRelays(relays = sessions.flatMap { it.relays }.toSet())
                }
        }

    private fun observeRelayEvents() =
        scope.launch {
            nostrRelayManager.relayEvents.collect { event ->
                when (event) {
                    is RelayEvent.Connected -> {
                        relaySessionMap[event.relayUrl]?.let {
                            sessionRepository.incrementActiveRelayCount(sessionIds = it)
                        }
                    }

                    is RelayEvent.Disconnected -> {
                        relaySessionMap[event.relayUrl]?.let {
                            sessionRepository.decrementActiveRelayCountOrEnd(sessionIds = it)
                        }
                    }
                }
            }
        }

    private fun observeMethods() =
        scope.launch {
            nostrRelayManager.incomingMethods.collect { method ->
                processMethod(method = method)
            }
        }

    private fun observeErrors() =
        scope.launch {
            nostrRelayManager.errors.collect { error ->
                sendResponse(response = error)
            }
        }

    private fun processMethod(method: RemoteSignerMethod) =
        scope.launch {
            if (!activeClientPubKeys.contains(method.clientPubKey)) return@launch

            val response = remoteSignerMethodResponseBuilder.build(method)

            sendResponse(response = response)
        }

    private suspend fun sendResponse(response: RemoteSignerMethodResponse) {
        nostrRelayManager.sendResponse(
            relays = connectionRepository
                .getConnectionByClientPubKey(clientPubKey = response.clientPubKey)
                .getOrNull()?.relays
                ?: return,
            response = response,
        )
    }

    override fun stop() {
        scope.launch { sessionRepository.endAllActiveSessions() }
            .invokeOnCompletion { scope.cancel() }
        nostrRelayManager.disconnectFromAll()
    }
}
