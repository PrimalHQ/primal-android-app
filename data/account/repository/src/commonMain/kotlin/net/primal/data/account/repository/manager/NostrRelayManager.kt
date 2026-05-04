package net.primal.data.account.repository.manager

import io.github.aakira.napier.Napier
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.Result
import net.primal.core.utils.cache.LruSeenCache
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.getAndClear
import net.primal.core.utils.onFailure
import net.primal.core.utils.put
import net.primal.core.utils.remove
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.CommonJsonImplicitNulls
import net.primal.data.account.repository.manager.model.RelayEvent
import net.primal.data.account.signer.remote.RemoteSignerClient
import net.primal.data.account.signer.remote.model.RemoteSignerMethod
import net.primal.data.account.signer.remote.model.RemoteSignerMethodException
import net.primal.data.account.signer.remote.model.RemoteSignerMethodResponse
import net.primal.data.account.signer.remote.parser.RemoteSignerMethodParser
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.assureValidNsec
import net.primal.domain.nostr.cryptography.utils.assureValidPubKeyHex

private const val MAX_CACHE_SIZE = 20
private const val REBROADCAST_COUNT = 5
private val REBROADCAST_DELAY = 2.seconds
private val REBROADCAST_TIMEOUT = 30.seconds
private val RECONNECT_DELAY_INITIAL = 1.seconds
private val RECONNECT_DELAY_MAX = 60.seconds

@OptIn(ExperimentalAtomicApi::class)
internal class NostrRelayManager(
    private val dispatcherProvider: DispatcherProvider,
    private val signerKeyPair: NostrKeyPair,
    private val nostrEncryptionService: NostrEncryptionService,
) {
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())

    private val clients = AtomicReference<Map<String, RemoteSignerClient>>(emptyMap())
    private val clientJobs = AtomicReference<Map<String, Job>>(emptyMap())
    private val reconnectJobs = AtomicReference<Map<String, Job>>(emptyMap())

    private val cache: LruSeenCache<String> = LruSeenCache(maxEntries = MAX_CACHE_SIZE)

    private val _incomingMethods = MutableSharedFlow<RemoteSignerMethod>(extraBufferCapacity = 64)
    val incomingMethods: Flow<RemoteSignerMethod> = _incomingMethods.asSharedFlow()

    private val _errors = MutableSharedFlow<RemoteSignerMethodException>(extraBufferCapacity = 64)
    val errors = _errors.asSharedFlow()

    private val _relayEvents = MutableSharedFlow<RelayEvent>(extraBufferCapacity = 64)
    val relayEvents = _relayEvents.asSharedFlow()

    suspend fun connectToRelays(relays: Set<String>) {
        Napier.d(tag = "Signer") { "Connecting to relays: $relays" }
        val currentKeys = clients.load().keys
        (currentKeys - relays).forEach { disconnectFromRelay(relay = it) }
        (relays - currentKeys).forEach {
            runCatching { connectToRelay(relay = it) }
                .onFailure { error ->
                    Napier.w(tag = "SignerNostrRelayManager", throwable = error) { "Failed to connect to relay: $it" }
                    scheduleReconnect(it)
                }
        }
    }

    fun reconnectToRelays(relays: Set<String>) {
        Napier.d(tag = "SignerNostrRelayManager") { "Reconnecting to relays: $relays" }
        val disconnectedRelays = relays - clients.load().keys
        disconnectedRelays.forEach { scheduleReconnect(relay = it) }
    }

    private suspend fun connectToRelay(relay: String) {
        val client = RemoteSignerClient(
            relayUrl = relay,
            dispatchers = dispatcherProvider,
            signerKeyPair = signerKeyPair,
            remoteSignerMethodParser = RemoteSignerMethodParser(nostrEncryptionService),
            onSocketConnectionOpened = { url ->
                Napier.d(tag = "SignerNostrRelayManager") { "Connected to relay: $url" }
                scope.launch { _relayEvents.emit(RelayEvent.Connected(relayUrl = url)) }
            },
            onSocketConnectionClosed = { url, _ ->
                Napier.d(tag = "SignerNostrRelayManager") { "Disconnected from relay: $url" }
                scope.launch {
                    _relayEvents.emit(RelayEvent.Disconnected(relayUrl = url))
                    removeClient(relay)
                    if (reconnectJobs.load()[relay]?.isActive != true) {
                        scheduleReconnect(relay)
                    }
                }
            },
        )

        client.connect().getOrThrow()

        observeClientMethods(
            relay = relay,
            client = client,
        )
    }

    private fun scheduleReconnect(relay: String) {
        val existingJob = reconnectJobs.load()[relay]
        existingJob?.cancel()
        val job = scope.launch {
            var reconnectDelay = RECONNECT_DELAY_INITIAL
            while (isActive) {
                Napier.d(tag = "SignerNostrRelayManager") { "Scheduling reconnect to relay $relay in $reconnectDelay" }
                delay(reconnectDelay)

                if (clients.load().containsKey(relay)) break

                Napier.d(tag = "SignerNostrRelayManager") { "Attempting reconnect to relay: $relay" }
                runCatching { connectToRelay(relay) }
                    .onFailure {
                        Napier.w(
                            tag = "SignerNostrRelayManager",
                            throwable = it,
                        ) { "Failed to reconnect to relay: $relay" }
                    }

                if (clients.load().containsKey(relay)) {
                    Napier.d(tag = "SignerNostrRelayManager") { "Reconnected to relay: $relay" }
                    break
                }
                reconnectDelay = (reconnectDelay * 2).coerceAtMost(RECONNECT_DELAY_MAX)
            }
        }
        reconnectJobs.put(key = relay, value = job)
    }

    fun disconnectFromRelay(relay: String) =
        scope.launch {
            reconnectJobs.load()[relay]?.cancel()
            reconnectJobs.remove(key = relay)
            removeClient(relay)
        }

    suspend fun disconnectFromAll() {
        val oldReconnectJobs = reconnectJobs.getAndClear()
        oldReconnectJobs.values.forEach { it.cancel() }

        val oldJobs = clientJobs.getAndClear()
        oldJobs.values.forEach { it.cancel() }

        val oldClients = clients.getAndClear()
        oldClients.values.forEach { it.destroy() }

        scope.cancel()
    }

    fun sendResponse(
        relays: List<String>,
        response: RemoteSignerMethodResponse,
        rebroadcast: Boolean = false,
    ) = runCatching {
        Napier.d(tag = "Signer") { "Broadcast: $rebroadcast; Sending response: $response" }
        val event = buildSignedEvent(response = response)
            .onFailure {
                Napier.w(tag = "Signer", throwable = it) {
                    "Failed to sign event. Something must have gone horribly wrong."
                }
            }.getOrThrow()

        if (rebroadcast) {
            rebroadcastEvent(event = event, relays = relays)
        }

        val currentClients = clients.load()
        val activeClients = relays.mapNotNull { relay -> currentClients[relay] }
            .also { clients ->
                if (clients.isEmpty()) {
                    error("We don't have active connection to any of the following relays: $relays")
                }
            }

        activeClients.forEach { client ->
            scope.launch {
                client.publishEvent(event = event)
            }
        }
    }

    private fun rebroadcastEvent(event: NostrEvent, relays: List<String>) {
        scope.launch {
            Napier.d(tag = "Signer") { "Rebroadcasting event: ${event.id}" }
            withTimeoutOrNull(REBROADCAST_TIMEOUT) {
                repeat(REBROADCAST_COUNT) {
                    delay(REBROADCAST_DELAY)
                    val currentClients = clients.load()
                    relays.mapNotNull { relay -> currentClients[relay] }
                        .forEach { client ->
                            launch {
                                client.publishEvent(event = event)
                                    .onFailure {
                                        Napier.w(tag = "Signer", throwable = it) {
                                            "Rebroadcast failed for event: ${event.id}"
                                        }
                                    }
                            }
                        }
                }
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
        clients.put(key = relay, value = client)
        val job = scope.launch {
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
                    if (cache.seen(error.nostrEvent.id)) return@collect

                    if (!_errors.tryEmit(error)) {
                        _errors.emit(error)
                    }

                    cache.mark(error.nostrEvent.id)
                }
            }
        }
        clientJobs.put(key = relay, value = job)
    }

    private suspend fun removeClient(relay: String) {
        clients.remove(key = relay)[relay]?.destroy()
        clientJobs.remove(key = relay)[relay]?.cancel()
    }
}
