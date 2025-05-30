package net.primal.core.networking.nwc

import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.networking.nwc.model.NwcWalletRequest
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.networking.sockets.filterBySubscriptionId
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.nostr.publisher.NostrEventPublisher
import net.primal.domain.nostr.publisher.NostrPublishException
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.ZapFailureException
import net.primal.domain.nostr.zaps.ZapRequestData

internal class NwcClientImpl(
    private val nwcData: NostrWalletConnect,
    private val nwcZapHelper: NwcZapHelper,
) : NostrZapper, NwcApi, NostrEventPublisher {

    private companion object {
        val TIMEOUT = 15.seconds
    }

    private val nwcRelaySocketClient by lazy {
        val relayUrl = nwcData.relays.first()
        NostrSocketClientFactory.create(wssUrl = relayUrl)
    }

    override suspend fun zap(data: ZapRequestData) {
        val zapPayRequest = nwcZapHelper.fetchZapPayRequestOrThrow(data.lnUrlDecoded)

        val invoice = nwcZapHelper.fetchInvoiceOrThrow(
            zapPayRequest = zapPayRequest,
            zapEvent = data.userZapRequestEvent,
            satoshiAmountInMilliSats = data.zapAmountInSats * 1000.toULong(),
            comment = data.zapComment,
        )

        try {
            publishNostrEvent(
                nostrEvent = signNwcRequestNostrEvent(
                    nwc = nwcData,
                    request = invoice.toWalletPayRequest(),
                ).unwrapOrThrow(),
            )
        } catch (error: NostrPublishException) {
            throw ZapFailureException(cause = error)
        } catch (error: SignatureException) {
            throw ZapFailureException(cause = error)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getBalance(): NwcResult<Long> =
        try {
            val requestEvent = signNwcRequestNostrEvent(
                nwc = nwcData,
                request = NwcWalletRequest<JsonObject>(
                    method = "get_balance",
                    params = buildJsonObject {
                        put("test", true)
                    },
                ),
            ).unwrapOrThrow()

            nwcRelaySocketClient.ensureSocketConnectionOrThrow()
            val responseEvent = withTimeoutOrNull(TIMEOUT) {
                val resultDeferred = CompletableDeferred<NostrEvent>()
                val responseListenerJob = launch {
                    val listenerId = Uuid.random().toPrimalSubscriptionId()
                    nwcRelaySocketClient.sendREQ(
                        subscriptionId = listenerId,
                        data = buildJsonObject {
                            put("kinds", buildJsonArray { add(23_195) })
                            put("#e", buildJsonArray { add(requestEvent.id) })
                        },
                    )
                    try {
                        nwcRelaySocketClient.incomingMessages.filterBySubscriptionId(id = listenerId).collect {
                            if (it is NostrIncomingMessage.EventMessage) {
                                it.nostrEvent?.let {
                                    resultDeferred.complete(it)
                                }
                            }
                        }
                    } catch (error: Exception) {
                        resultDeferred.completeExceptionally(error)
                    }
                }

                try {
                    nwcRelaySocketClient.sendEVENT(signedEvent = requestEvent.toNostrJsonObject())
                    resultDeferred.await()
                } finally {
                    responseListenerJob.cancel()
                }
            }

            if (responseEvent != null) {
                Napier.e { responseEvent.toString() }

                // TODO Decrypt response event, read the info and return it
            }

            NwcResult.Success(result = 0L)
        } catch (error: Exception) {
            Napier.e(error) { "Failed to get balance" }
            NwcResult.Failure(error = error)
        }

    override suspend fun getTransactions(): NwcResult<List<JsonObject>> {
        return NwcResult.Success(result = emptyList())
    }

    override suspend fun publishNostrEvent(nostrEvent: NostrEvent, outboxRelays: List<String>) {
        nwcRelaySocketClient.ensureSocketConnectionOrThrow()
        nwcRelaySocketClient.sendEVENT(nostrEvent.toNostrJsonObject())
    }
}
