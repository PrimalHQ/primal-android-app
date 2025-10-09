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
import net.primal.core.lightning.LightningPayHelper
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
import net.primal.core.networking.nwc.nip47.NwcWalletRequest
import net.primal.core.networking.nwc.nip47.PayInvoiceParams
import net.primal.core.networking.nwc.nip47.PayInvoiceResponsePayload
import net.primal.core.networking.nwc.nip47.PayKeysendParams
import net.primal.core.networking.nwc.nip47.PayKeysendResponsePayload
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.networking.sockets.filterBySubscriptionId
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.MSATS_IN_SATS
import net.primal.core.utils.Result
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.nostr.publisher.NostrEventPublisher
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapRequestData
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.wallet.NostrWalletConnect

internal class NwcClientImpl(
    private val nwcData: NostrWalletConnect,
    private val lightningPayHelper: LightningPayHelper?,
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
    ): Result<R> {
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
                    Result.success(it)
                } ?: Result.failure(NetworkException("NWC Error: ${parsed.error?.message}"))
            } else {
                Result.failure(NetworkException("No response event received."))
            }
        } catch (e: Exception) {
            Napier.e(e) { "NWC request failed: $method" }
            Result.failure(NetworkException("Failed to execute NWC request: $method", e))
        }
    }

    override suspend fun zap(walletId: String, data: ZapRequestData): ZapResult {
        val nwcZapHelper = lightningPayHelper ?: throw IllegalStateException(
            "NwcZapHelper is required when using NwcClientImpl as a NostrZapper. " +
                "Please provide NwcZapHelper instance in the constructor.",
        )

        val zapPayRequest = runCatching {
            nwcZapHelper.fetchPayRequest(data.recipientLnUrlDecoded)
        }.getOrElse {
            Napier.e(it) { "FailedToFetchZapPayRequest." }
            return ZapResult.Failure(error = ZapError.FailedToFetchZapPayRequest(cause = it))
        }

        val invoice = runCatching {
            nwcZapHelper.fetchInvoice(
                payRequest = zapPayRequest,
                amountInMilliSats = data.zapAmountInSats * MSATS_IN_SATS.toULong(),
                comment = data.zapComment,
                zapEvent = data.userZapRequestEvent,
            )
        }.getOrElse {
            Napier.e(it) { "FailedToFetchZapInvoice." }
            return ZapResult.Failure(error = ZapError.FailedToFetchZapInvoice(cause = it))
        }

        val nostrEvent = runCatching {
            signNwcRequestNostrEvent(
                nwc = nwcData,
                request = NwcWalletRequest(
                    method = "pay_invoice",
                    params = PayInvoiceParams(invoice = invoice.invoice),
                ),
            ).unwrapOrThrow()
        }.getOrElse {
            Napier.e(it) { "FailedToSignEvent." }
            return ZapResult.Failure(error = ZapError.FailedToSignEvent)
        }

        runCatching {
            publishNostrEvent(nostrEvent)
        }.getOrElse {
            Napier.e(it) { "FailedToPublishEvent." }
            return ZapResult.Failure(error = ZapError.FailedToPublishEvent)
        }

        return ZapResult.Success
    }

    override suspend fun getBalance(): Result<GetBalanceResponsePayload> =
        sendNwcRequest(method = NwcMethod.GetBalance.value, params = buildJsonObject {})

    override suspend fun listTransactions(params: ListTransactionsParams): Result<ListTransactionsResponsePayload> =
        sendNwcRequest(method = NwcMethod.ListTransactions.value, params = params)

    override suspend fun makeInvoice(params: MakeInvoiceParams): Result<MakeInvoiceResponsePayload> =
        sendNwcRequest(method = NwcMethod.MakeInvoice.value, params = params)

    override suspend fun lookupInvoice(params: LookupInvoiceParams): Result<LookupInvoiceResponsePayload> =
        sendNwcRequest(method = NwcMethod.LookupInvoice.value, params = params)

    override suspend fun getInfo(): Result<GetInfoResponsePayload> =
        sendNwcRequest(method = NwcMethod.GetInfo.value, params = buildJsonObject {})

    override suspend fun payInvoice(params: PayInvoiceParams): Result<PayInvoiceResponsePayload> =
        sendNwcRequest(method = NwcMethod.PayInvoice.value, params = params)

    override suspend fun payKeysend(params: PayKeysendParams): Result<PayKeysendResponsePayload> =
        sendNwcRequest(method = NwcMethod.PayKeysend.value, params = params)

    override suspend fun multiPayInvoice(params: List<PayInvoiceParams>): Result<List<PayInvoiceResponsePayload>> =
        sendNwcRequest(method = NwcMethod.MultiPayInvoice.value, params = params)

    override suspend fun multiPayKeysend(params: List<PayKeysendParams>): Result<List<PayKeysendResponsePayload>> =
        sendNwcRequest(method = NwcMethod.MultiPayKeysend.value, params = params)

    override suspend fun publishNostrEvent(nostrEvent: NostrEvent, outboxRelays: List<String>) {
        nwcRelaySocketClient.ensureSocketConnectionOrThrow()
        nwcRelaySocketClient.sendEVENT(nostrEvent.toNostrJsonObject())
    }
}
