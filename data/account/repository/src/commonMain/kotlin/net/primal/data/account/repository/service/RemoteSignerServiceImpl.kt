package net.primal.data.account.repository.service

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.primal.data.account.remote.command.model.NostrCommand
import net.primal.data.account.repository.handler.NostrCommandHandler
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrKeyPair

class RemoteSignerServiceImpl internal constructor(
    private val signerKeyPair: NostrKeyPair,
    private val connectionRepository: ConnectionRepository,
    private val nostrRelayManager: NostrRelayManager,
    private val nostrCommandHandler: NostrCommandHandler,
) : RemoteSignerService {

    private val scope = CoroutineScope(SupervisorJob())

    override fun start() {
        scope.launch {
            Napier.d(tag = "Signer") { "RemoteSignerService started." }
            /* TODO(marko): this should be observable, good enough for now. */
            connectionRepository.getAllConnections(signerPubKey = signerKeyPair.pubKey)
                .flatMap { it.relays }
                .toSet()
                .let { relays ->
                    nostrRelayManager.connectToRelays(relays)
                }

            observeEvents()
        }
    }

    private fun observeEvents() =
        scope.launch {
            nostrRelayManager.incomingCommands.collect { command ->
                processCommand(command = command)
            }
        }

    /*
        TODO(marko): we should make sure we ALWAYS send back some kind of response.
            Whatever happens in this chain before this, should be propagated.
            Maybe we should have another flow for observing errors across clients? food for thought.
      */
    private fun processCommand(command: NostrCommand) =
        scope.launch {
            val response = nostrCommandHandler.handle(command)

            nostrRelayManager.sendResponse(
                relays = connectionRepository
                    .getConnectionByClientPubKey(clientPubKey = command.clientPubKey)
                    .getOrNull()?.relays
                    ?: return@launch,
                clientPubKey = command.clientPubKey,
                response = response,
            )
        }

    override fun stop() {
        nostrRelayManager.disconnectFromAll()
        scope.cancel()
    }
}
