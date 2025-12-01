package net.primal.data.account.repository.manager

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.cache.LruSeenCache
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.CommonJsonImplicitNulls
import net.primal.data.account.remote.client.RemoteSignerClient
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.method.processor.RemoteSignerMethodProcessor
import net.primal.data.account.repository.manager.model.RelayEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.assureValidNsec
import net.primal.domain.nostr.cryptography.utils.assureValidPubKeyHex

private const val MAX_CACHE_SIZE = 20

internal class NostrRelayManager(
    private val dispatcherProvider: DispatcherProvider,
    private val signerKeyPair: NostrKeyPair,
    private val nostrEncryptionService: NostrEncryptionService,
) {
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())

    private val clients: MutableMap<String, RemoteSignerClient> = mutableMapOf()
    private val clientJobs: MutableMap<String, Job> = mutableMapOf()

    private val cache: LruSeenCache<String> = LruSeenCache(maxEntries = MAX_CACHE_SIZE)

    private val _incomingMethods = MutableSharedFlow<RemoteSignerMethod>(extraBufferCapacity = 64)
    val incomingMethods: Flow<RemoteSignerMethod> = _incomingMethods.asSharedFlow()

    private val _errors = MutableSharedFlow<RemoteSignerMethodResponse.Error>(extraBufferCapacity = 64)
    val errors = _errors.asSharedFlow()

    private val _relayEvents = MutableSharedFlow<RelayEvent>(extraBufferCapacity = 64)
    val relayEvents = _relayEvents.asSharedFlow()

    suspend fun connectToRelays(relays: Set<String>) {
        Napier.d(tag = "Signer") { "Connecting to relays: $relays" }
        (clients.keys - relays).forEach { disconnectFromRelay(relay = it) }
        (relays - clients.keys).forEach { connectToRelay(relay = it) }
    }

    suspend fun connectToRelay(relay: String) {
        val client = RemoteSignerClient(
            relayUrl = relay,
            dispatchers = dispatcherProvider,
            signerKeyPair = signerKeyPair,
            remoteSignerMethodProcessor = RemoteSignerMethodProcessor(nostrEncryptionService),
            onSocketConnectionOpened = { url ->
                Napier.d(tag = "SignerNostrRelayManager") { "Connected to relay: $url" }
                scope.launch { _relayEvents.emit(RelayEvent.Connected(relayUrl = url)) }
            },
            onSocketConnectionClosed = { url, _ ->
                Napier.d(tag = "SignerNostrRelayManager") { "Disconnected from relay: $url" }
                scope.launch { _relayEvents.emit(RelayEvent.Disconnected(relayUrl = url)) }
                clients.remove(relay)
                clientJobs.remove(relay)?.cancel()
            },
        )

        client.connect()

        observeClientMethods(
            relay = relay,
            client = client,
        )
    }

    fun disconnectFromRelay(relay: String) = scope.launch { removeClient(relay) }

    suspend fun disconnectFromAll() {
        clientJobs.values.forEach { it.cancel() }
        clientJobs.clear()
        clients.values.forEach { it.destroy() }
        clients.clear()
        scope.cancel()
    }

    fun sendResponse(relays: List<String>, response: RemoteSignerMethodResponse) {
        Napier.d(tag = "Signer") { "Sending response: $response" }
        buildSignedEvent(response = response)
            .onSuccess { event ->
                relays.mapNotNull { relay -> clients[relay] }
                    .forEach { client ->
                        scope.launch {
                            client.publishEvent(event = event)
                        }
                    }
            }.onFailure {
                Napier.w(tag = "Signer", throwable = it) {
                    "Failed to sign event. Something must have gone horribly wrong."
                }
            }
    }

    private fun buildSignedEvent(response: RemoteSignerMethodResponse): Result<NostrEvent> =
        runCatching {
            NostrUnsignedEvent(
                pubKey = signerKeyPair.pubKey.assureValidPubKeyHex(),
                tags = listOf(response.clientPubKey.asPubkeyTag()),
                kind = NostrEventKind.NostrConnect.value,
                content = nostrEncryptionService.nip44Encrypt(
                    plaintext = CommonJsonImplicitNulls.encodeToString(response),
                    privateKey = signerKeyPair.privateKey,
                    pubKey = response.clientPubKey,
                ).getOrThrow(),
            ).signOrThrow(nsec = signerKeyPair.privateKey.assureValidNsec())
        }

    private fun observeClientMethods(relay: String, client: RemoteSignerClient) {
        clients[relay] = client
        clientJobs[relay] = scope.launch {
            launch {
                client.incomingMethods.collect { method ->
                    Napier.d(tag = "Signer") { "Got method in `NostrRelayManager`: $method" }
                    if (cache.seen(method.id)) return@collect
                    Napier.d(tag = "Signer") { "We didn't previously see this method." }

                    if (!_incomingMethods.tryEmit(method)) {
                        _incomingMethods.emit(method)
                    }

                    Napier.d(tag = "Signer") { "Emitted method: $method" }
                    cache.mark(method.id)
                }
            }

            launch {
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

    private suspend fun removeClient(relay: String) {
        clients.remove(relay)?.destroy()
        clientJobs.remove(relay)?.cancel()
    }
}
