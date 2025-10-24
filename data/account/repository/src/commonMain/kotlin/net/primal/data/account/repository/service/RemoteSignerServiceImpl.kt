package net.primal.data.account.repository.service

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.repository.handler.RemoteSignerMethodResponseBuilder
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrKeyPair

class RemoteSignerServiceImpl internal constructor(
    private val signerKeyPair: NostrKeyPair,
    private val connectionRepository: ConnectionRepository,
    private val nostrRelayManager: NostrRelayManager,
    private val remoteSignerMethodResponseBuilder: RemoteSignerMethodResponseBuilder,
) : RemoteSignerService {

    private val scope = CoroutineScope(SupervisorJob())

    override fun start() {
        Napier.d(tag = "Signer") { "RemoteSignerService started." }
        observeConnections()
        observeMethods()
    }

    private fun observeConnections() =
        scope.launch {
            connectionRepository.observeAllConnections(signerPubKey = signerKeyPair.pubKey)
                .map { connections -> connections.flatMap { it.relays }.toSet() }
                .collect { relays ->
                    nostrRelayManager.connectToRelays(relays)
                }
        }

    private fun observeMethods() =
        scope.launch {
            nostrRelayManager.incomingMethods.collect { method ->
                processMethod(method = method)
            }
        }

    private fun processMethod(method: RemoteSignerMethod) =
        scope.launch {
            val response = remoteSignerMethodResponseBuilder.build(method)

            nostrRelayManager.sendResponse(
                relays = connectionRepository
                    .getConnectionByClientPubKey(clientPubKey = method.clientPubKey)
                    .getOrNull()?.relays
                    ?: return@launch,
                clientPubKey = method.clientPubKey,
                response = response,
            )
        }

    override fun stop() {
        nostrRelayManager.disconnectFromAll()
        scope.cancel()
    }
}
