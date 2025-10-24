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
import net.primal.core.networking.sockets.filterBySubscriptionId
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.CommonJsonImplicitNulls
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.method.parser.RemoteSignerMethodParser
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.signOrThrow
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
) {
    private val scope = CoroutineScope(dispatchers.io() + SupervisorJob())
    private val nostrSocketClient = NostrSocketClientFactory.create(wssUrl = relayUrl)

    private val remoteSignerMethodParser = RemoteSignerMethodParser()
    private val nip44 = Nip44v2()

    private val _incomingMethods: Channel<RemoteSignerMethod> = Channel()
    val incomingMethods = _incomingMethods.receiveAsFlow()

    private var listenerJob: Job? = null

    fun connect() =
        scope.launch {
            runCatching {
                nostrSocketClient.ensureSocketConnectionOrThrow()
                startSubscription()
            }
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

    suspend fun publishResponse(clientPubKey: String, response: RemoteSignerMethodResponse): Result<Unit> =
        runCatching {
            Napier.d(tag = "Signer") { "Sending response: $response" }
            nostrSocketClient.sendEVENT(
                signedEvent = NostrUnsignedEvent(
                    pubKey = signerKeyPair.pubKey.assureValidPubKeyHex(),
                    tags = listOf(clientPubKey.asPubkeyTag()),
                    kind = NostrEventKind.NostrConnect.value,
                    content = nip44.encrypt(
                        msg = CommonJsonImplicitNulls.encodeToString(response),
                        privateKey = signerKeyPair.privateKey.assureValidNsec()
                            .bechToBytesOrThrow(),
                        pubKey = clientPubKey.assureValidNpub().bechToBytesOrThrow(),
                    ).encodePayload(),
                ).signOrThrow(nsec = signerKeyPair.privateKey)
                    .toNostrJsonObject(),
            )
        }.onFailure {
            Napier.w(tag = "Signer", throwable = it) { "Failed to publish event." }
        }

    private fun processEvent(event: NostrEvent) =
        scope.launch {
            val decryptedContent = event.decryptContent()

            remoteSignerMethodParser.parse(clientPubkey = event.pubKey, content = decryptedContent)
                .onSuccess { command ->
                    _incomingMethods.send(command)
                }.onFailure {
                    Napier.w(throwable = it) { "Couldn't parse received event." }
                }
        }

    private fun NostrEvent.decryptContent(): String =
        nip44.decrypt(
            payload = this.content,
            privateKey = signerKeyPair.privateKey.assureValidNsec().bechToBytesOrThrow(),
            pubKey = this.pubKey.assureValidNpub().bechToBytesOrThrow(),
        )
}
