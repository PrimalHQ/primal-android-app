package net.primal.core.networking.nwc

import fr.acinq.secp256k1.Hex
import io.github.aakira.napier.Napier
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.networking.nwc.model.NwcWalletRequest
import net.primal.core.networking.nwc.nip47.GetBalanceResponsePayload
import net.primal.core.networking.nwc.nip47.GetInfoResponsePayload
import net.primal.core.networking.nwc.nip47.ListTransactionsParams
import net.primal.core.networking.nwc.nip47.ListTransactionsResponsePayload
import net.primal.core.networking.nwc.nip47.LookupInvoiceParams
import net.primal.core.networking.nwc.nip47.LookupInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.MakeInvoiceParams
import net.primal.core.networking.nwc.nip47.MakeInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.NwcMethod
import net.primal.core.networking.nwc.nip47.NwcResponseContent
import net.primal.core.networking.nwc.nip47.PayInvoiceParams
import net.primal.core.networking.nwc.nip47.PayInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.PayKeysendParams
import net.primal.core.networking.nwc.nip47.PayKeysendResponsePayload
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.networking.sockets.filterBySubscriptionId
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
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

    @OptIn(ExperimentalUuidApi::class, ExperimentalEncodingApi::class)
    private suspend inline fun <reified T : Any, reified R : Any> sendNwcRequest(
        method: String,
        params: T,
    ): NwcResult<R> {
        return try {
            val requestEvent = signNwcRequestNostrEvent(
                nwc = nwcData,
                request = NwcWalletRequest(method = method, params = params),
            ).unwrapOrThrow()

            nwcRelaySocketClient.ensureSocketConnectionOrThrow()

            val responseEvent = withTimeoutOrNull(TIMEOUT) {
                val resultDeferred = CompletableDeferred<NostrEvent>()
                val listenerId = Uuid.random().toPrimalSubscriptionId()

                val responseListenerJob = launch {
                    nwcRelaySocketClient.sendREQ(
                        subscriptionId = listenerId,
                        data = buildJsonObject {
                            put("kinds", buildJsonArray { add(NostrEventKind.NwcResponse.value) })
                            put("#e", buildJsonArray { add(requestEvent.id) })
                        },
                    )
                    try {
                        nwcRelaySocketClient.incomingMessages.filterBySubscriptionId(id = listenerId).collect {
                            if (it is NostrIncomingMessage.EventMessage) {
                                it.nostrEvent?.let { event ->
                                    resultDeferred.complete(event)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        resultDeferred.completeExceptionally(e)
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
                val decrypted = CryptoUtils.decrypt(
                    message = responseEvent.content,
                    privateKey = Hex.decode(nwcData.keypair.privateKey),
                    pubKey = Hex.decode(nwcData.pubkey),
                )
                val parsed = CommonJson.decodeFromString<NwcResponseContent<R>>(decrypted)
                parsed.result?.let {
                    NwcResult.Success(it)
                } ?: NwcResult.Failure(Exception("NWC Error: ${parsed.error?.message}"))
            } else {
                NwcResult.Failure(Exception("No response event received."))
            }
        } catch (e: Exception) {
            Napier.e(e) { "NWC request failed: $method" }
            NwcResult.Failure(e)
        }
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

    override suspend fun getBalance(): NwcResult<GetBalanceResponsePayload> =
        sendNwcRequest(method = NwcMethod.GetBalance.value, params = buildJsonObject {})

    override suspend fun listTransactions(params: ListTransactionsParams): NwcResult<ListTransactionsResponsePayload> =
        sendNwcRequest(method = NwcMethod.ListTransactions.value, params = params)

    override suspend fun makeInvoice(params: MakeInvoiceParams): NwcResult<MakeInvoiceResponsePayload> =
        sendNwcRequest(method = NwcMethod.MakeInvoice.value, params = params)

    override suspend fun lookupInvoice(params: LookupInvoiceParams): NwcResult<LookupInvoiceResponsePayload> =
        sendNwcRequest(method = NwcMethod.LookupInvoice.value, params = params)

    override suspend fun getInfo(): NwcResult<GetInfoResponsePayload> =
        sendNwcRequest(method = NwcMethod.GetInfo.value, params = buildJsonObject {})

    override suspend fun payInvoice(params: PayInvoiceParams): NwcResult<PayInvoiceResponsePayload> =
        sendNwcRequest(method = NwcMethod.PayInvoice.value, params = params)

    override suspend fun payKeysend(params: PayKeysendParams): NwcResult<PayKeysendResponsePayload> =
        sendNwcRequest(method = NwcMethod.PayKeysend.value, params = params)

    override suspend fun multiPayInvoice(params: List<PayInvoiceParams>): NwcResult<List<PayInvoiceResponsePayload>> =
        sendNwcRequest(method = NwcMethod.MultiPayInvoice.value, params = params)

    override suspend fun multiPayKeysend(params: List<PayKeysendParams>): NwcResult<List<PayKeysendResponsePayload>> =
        sendNwcRequest(method = NwcMethod.MultiPayKeysend.value, params = params)

    override suspend fun publishNostrEvent(nostrEvent: NostrEvent, outboxRelays: List<String>) {
        nwcRelaySocketClient.ensureSocketConnectionOrThrow()
        nwcRelaySocketClient.sendEVENT(nostrEvent.toNostrJsonObject())
    }
}
