package net.primal.data.account.repository.manager

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.primal.core.utils.cache.LruSeenCache
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.account.remote.client.RemoteSignerClient
import net.primal.data.account.remote.command.model.NostrCommand
import net.primal.data.account.remote.command.model.NostrCommandResponse
import net.primal.domain.nostr.cryptography.NostrKeyPair

private const val MAX_CACHE_SIZE = 20

/*
    TODO(marko): This is a long running service. Bad network conditions must be considered.
     We should have a way of detecting disconnect from some relay and some kind of fallback logic should be implemented?
     Right now we are assuming happy path. We connected and are staying connected to relays.
     What happens when we suddenly get disconnected?
 */
internal class NostrRelayManager(
    private val dispatcherProvider: DispatcherProvider,
    private val signerKeyPair: NostrKeyPair,
) {
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())

    private val clients: MutableMap<String, RemoteSignerClient> = mutableMapOf()
    private val clientJobs: MutableMap<String, Job> = mutableMapOf()

    private val cache: LruSeenCache<String> = LruSeenCache(maxEntries = MAX_CACHE_SIZE)

    private val _incomingCommands = MutableSharedFlow<NostrCommand>(
        replay = 0,
        extraBufferCapacity = 64,
    )
    val incomingCommands: Flow<NostrCommand> = _incomingCommands.asSharedFlow()

    fun connectToRelays(relays: Set<String>) {
        Napier.d(tag = "Signer") { "Connecting to relays: $relays" }
        (relays - clients.keys).forEach { connectToRelay(relay = it) }
    }

    fun connectToRelay(relay: String) {
        val client = RemoteSignerClient(
            relayUrl = relay,
            dispatchers = dispatcherProvider,
            signerKeyPair = signerKeyPair,
        )

        client.connect()

        addClient(
            relay = relay,
            client = client,
        )
    }

    fun disconnectFromRelay(relay: String) = scope.launch { removeClient(relay) }

    fun disconnectFromAll() {
        clientJobs.values.forEach { it.cancel() }
        clientJobs.clear()
        clients.values.forEach { it.close() }
        clients.clear()
    }

    fun sendResponse(relays: List<String>, clientPubKey: String, response: NostrCommandResponse) {
        relays.mapNotNull { relay -> clients[relay] }
            .forEach { client ->
                /* TODO(marko): we should pay attention to errors here and propagate them. */
                scope.launch {
                    client.publishResponse(clientPubKey = clientPubKey, response = response)
                }
            }

    }

    private fun addClient(relay: String, client: RemoteSignerClient) {
        removeClient(relay)

        clients[relay] = client
        clientJobs[relay] = scope.launch {
            runCatching {
                client.incomingCommands.collect { cmd ->
                    if (cache.seen(cmd.id)) return@collect

                    if (!_incomingCommands.tryEmit(cmd)) {
                        _incomingCommands.emit(cmd)
                    }

                    cache.mark(cmd.id)
                }
            }.onFailure { error ->
                Napier.d(throwable = error) {
                    "Failed to emit event to joined `incomingCommands`. Something must have gone horribly wrong."
                }
            }
        }
    }

    private fun removeClient(relay: String) {
        clients[relay]?.close()
        clientJobs.remove(relay)?.cancel()
        clients.remove(relay)
    }
}
