package net.primal.data.account.signer.remote.client

import io.github.aakira.napier.Napier
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.networking.sockets.SocketConnectionClosedCallback
import net.primal.core.networking.sockets.SocketConnectionOpenedCallback
import net.primal.core.networking.sockets.filterBySubscriptionId
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.fold
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching
import net.primal.data.account.signer.remote.mappers.mapAsRemoteSignerMethodException
import net.primal.data.account.signer.remote.model.RemoteSignerMethod
import net.primal.data.account.signer.remote.model.RemoteSignerMethodException
import net.primal.data.account.signer.remote.parser.RemoteSignerMethodParser
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.utils.assureValidPubKeyHex
import net.primal.domain.nostr.serialization.toNostrJsonObject

class RemoteSignerClient(
    relayUrl: String,
    dispatchers: DispatcherProvider,
    private val signerKeyPair: NostrKeyPair,
    private val remoteSignerMethodParser: RemoteSignerMethodParser,
    onSocketConnectionOpened: SocketConnectionOpenedCallback? = null,
    onSocketConnectionClosed: SocketConnectionClosedCallback? = null,
) {
    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())
    private val nostrSocketClient = NostrSocketClientFactory.create(
        wssUrl = relayUrl,
        onSocketConnectionOpened = onSocketConnectionOpened,
        onSocketConnectionClosed = onSocketConnectionClosed,
    )

    private val _incomingMethods: Channel<RemoteSignerMethod> = Channel()
    val incomingMethods = _incomingMethods.receiveAsFlow()

    private val _errors: Channel<RemoteSignerMethodException> = Channel()
    val errors = _errors.receiveAsFlow()

    private var listenerJob: Job? = null

    suspend fun connect() =
        runCatching {
            nostrSocketClient.ensureSocketConnectionOrThrow()
            startSubscription()
        }

    private fun startSubscription() {
        listenerJob?.cancel()
        listenerJob = scope.launch {
            val listenerId = Uuid.random().toPrimalSubscriptionId()

            nostrSocketClient.sendREQ(
                subscriptionId = listenerId,
                data = buildJsonObject {
                    put("kinds", buildJsonArray { add(NostrEventKind.NostrConnect.value) })
                    put("#p", buildJsonArray { add(signerKeyPair.pubKey.assureValidPubKeyHex()) })
                },
            )

            runCatching {
                nostrSocketClient.incomingMessages.filterBySubscriptionId(id = listenerId)
                    .collect { message ->
                        if (message is NostrIncomingMessage.EventMessage) {
                            message.nostrEvent?.let { event ->
                                remoteSignerMethodParser.parseNostrEvent(
                                    event = event,
                                    signerKeyPair = signerKeyPair,
                                ).fold(
                                    onSuccess = { method ->
                                        scope.launch { _incomingMethods.send(method) }
                                    },
                                    onFailure = { error ->
                                        scope.launch { _errors.send(error.mapAsRemoteSignerMethodException(event)) }
                                    },
                                )
                            }
                        }
                    }
            }
        }
    }

    suspend fun destroy() {
        nostrSocketClient.close()
        scope.cancel()
    }

    suspend fun publishEvent(event: NostrEvent): Result<Unit> =
        runCatching {
            nostrSocketClient.ensureSocketConnectionOrThrow()
            nostrSocketClient.sendEVENT(signedEvent = event.toNostrJsonObject())
        }.onFailure {
            Napier.w(tag = "Signer", throwable = it) { "Failed to publish event." }
        }
}
