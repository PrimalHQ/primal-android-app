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
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.domain.nostr.cryptography.NostrKeyPair

private const val MAX_CACHE_SIZE = 20

internal class NostrRelayManager(
    private val dispatcherProvider: DispatcherProvider,
    private val signerKeyPair: NostrKeyPair,
) {
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())

    private val clients: MutableMap<String, RemoteSignerClient> = mutableMapOf()
    private val clientJobs: MutableMap<String, Job> = mutableMapOf()

    private val cache: LruSeenCache<String> = LruSeenCache(maxEntries = MAX_CACHE_SIZE)

    private val _incomingMethods = MutableSharedFlow<RemoteSignerMethod>(
        replay = 0,
        extraBufferCapacity = 64,
    )
    val incomingMethods: Flow<RemoteSignerMethod> = _incomingMethods.asSharedFlow()

    private val _errors = MutableSharedFlow<RemoteSignerMethodResponse.Error>(
        replay = 0,
        extraBufferCapacity = 64,
    )
    val errors = _errors.asSharedFlow()

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

        observeClientMethods(
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

    fun sendResponse(relays: List<String>, response: RemoteSignerMethodResponse) {
        relays.mapNotNull { relay -> clients[relay] }
            .forEach { client ->
                scope.launch {
                    client.publishResponse(response = response)
                }
            }
    }

    private fun observeClientMethods(relay: String, client: RemoteSignerClient) {
        removeClient(relay)

        clients[relay] = client
        clientJobs[relay] = scope.launch {
            scope.launch {
                client.incomingMethods.collect { method ->
                    if (cache.seen(method.id)) return@collect

                    if (!_incomingMethods.tryEmit(method)) {
                        _incomingMethods.emit(method)
                    }

                    cache.mark(method.id)
                }
            }

            scope.launch {
                client.errors.collect { error ->
                    if (cache.seen(error.id)) return@collect

                    if (!_errors.tryEmit(error)) {
                        _errors.emit(error)
                    }

                    cache.mark(error.id)
                }
            }
        }
    }

    private fun removeClient(relay: String) {
        clients.remove(relay)?.close()
        clientJobs.remove(relay)?.cancel()
    }
}
