package net.primal.data.account.repository.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.primal.data.account.remote.command.model.NostrCommand
import net.primal.data.account.repository.handler.NostrCommandHandler
import net.primal.data.account.repository.manager.NostrRelayManager
import net.primal.domain.account.model.AppConnection
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.service.RemoteSignerService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

class RemoteSignerServiceImpl internal constructor(
    eventSignatureHandler: NostrEventSignatureHandler,
    nostrEncryptionHandler: NostrEncryptionHandler,
    private val connectionRepository: ConnectionRepository,
    private val nostrRelayManager: NostrRelayManager,
) : RemoteSignerService {

    private val scope = CoroutineScope(SupervisorJob())
    private val nostrCommandHandler: NostrCommandHandler = NostrCommandHandler(
        nostrEventSignatureHandler = eventSignatureHandler,
        nostrEncryptionHandler = nostrEncryptionHandler,
        connectionRepository = connectionRepository,
    )

    /*
    TODO(marko): rethink this. Should we keep connections in memory?
         There are a few and could decrease db queries.
         Alternative would be to query db each time we want to respond to event. This might be heavy.
         Another alternative would be some smart caching but that would be overkill imo.
      */
    private var appConnections: List<AppConnection> = emptyList()

    override fun start() {
        scope.launch {
            /* TODO(marko): this should be observable, good enough for now. */
            connectionRepository.getAllConnections()
                .also { appConnections = it }
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

    private fun processCommand(command: NostrCommand) =
        scope.launch {
            val response = nostrCommandHandler.handle(command)

            nostrRelayManager.sendResponse(
                relays = appConnections
                    .firstOrNull { it.clientPubKey == command.clientPubKey }?.relays
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
