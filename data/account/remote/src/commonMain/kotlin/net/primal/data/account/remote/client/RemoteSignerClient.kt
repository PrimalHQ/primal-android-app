package net.primal.data.account.remote.client

import com.vitorpamplona.quartz.nip44Encryption.Nip44v2
import io.github.aakira.napier.Napier
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodRequest
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.method.parser.RemoteSignerMethodParser
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.utils.assureValidNpub
import net.primal.domain.nostr.cryptography.utils.assureValidNsec
import net.primal.domain.nostr.cryptography.utils.assureValidPubKeyHex
import net.primal.domain.nostr.cryptography.utils.bechToBytesOrThrow
import net.primal.domain.nostr.serialization.toNostrJsonObject

@OptIn(ExperimentalUuidApi::class)
class RemoteSignerClient(
    relayUrl: String,
    dispatchers: DispatcherProvider,
    private val signerKeyPair: NostrKeyPair,
    onSocketConnectionOpened: SocketConnectionOpenedCallback? = null,
    onSocketConnectionClosed: SocketConnectionClosedCallback? = null,
) {
    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())
    private val nostrSocketClient = NostrSocketClientFactory.create(
        wssUrl = relayUrl,
        onSocketConnectionOpened = onSocketConnectionOpened,
        onSocketConnectionClosed = onSocketConnectionClosed,
    )

    private val remoteSignerMethodParser = RemoteSignerMethodParser()
    private val nip44 = Nip44v2()

    private val _incomingMethods: Channel<RemoteSignerMethod> = Channel()
    val incomingMethods = _incomingMethods.receiveAsFlow()

    private val _errors: Channel<RemoteSignerMethodResponse.Error> = Channel()
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
                                processEvent(event = event)
                            }
                        }
                    }
            }
        }
    }

    fun close() =
        scope.launch {
            nostrSocketClient.close()
            listenerJob?.cancel()
        }

    suspend fun publishEvent(event: NostrEvent): Result<Unit> =
        runCatching {
            nostrSocketClient.sendEVENT(signedEvent = event.toNostrJsonObject())
        }.onFailure {
            Napier.w(tag = "Signer", throwable = it) { "Failed to publish event." }
        }

    private fun processEvent(event: NostrEvent) =
        scope.launch {
            val decryptedContent = runCatching { event.decryptContent() }
                .onFailure {
                    val message = "Failed to decrypt content. Raw: $event"
                    Napier.w(throwable = it) { message }
                    _errors.send(
                        RemoteSignerMethodResponse.Error(
                            id = event.id,
                            error = message,
                            clientPubKey = event.pubKey,
                        ),
                    )
                }.getOrNull() ?: return@launch

            remoteSignerMethodParser.parse(
                clientPubkey = event.pubKey,
                content = decryptedContent,
                requestedAt = event.createdAt,
            ).onSuccess { command ->
                Napier.d(tag = "Signer") { "Received command: $command" }
                _incomingMethods.send(command)
            }.onFailure {
                val message = it.message ?: "There was an error while parsing method."
                Napier.w(throwable = it) { message }
                val id = decryptedContent.decodeFromJsonStringOrNull<RemoteSignerMethodRequest>()?.id
                _errors.send(
                    RemoteSignerMethodResponse.Error(
                        id = id ?: event.id,
                        error = message,
                        clientPubKey = event.pubKey,
                    ),
                )
            }
        }

    private fun NostrEvent.decryptContent(): String =
        nip44.decrypt(
            payload = this.content,
            privateKey = signerKeyPair.privateKey.assureValidNsec().bechToBytesOrThrow(),
            pubKey = this.pubKey.assureValidNpub().bechToBytesOrThrow(),
        )
}
