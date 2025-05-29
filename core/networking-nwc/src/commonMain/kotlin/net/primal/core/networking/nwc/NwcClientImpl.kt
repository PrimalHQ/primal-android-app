package net.primal.core.networking.nwc

import fr.acinq.secp256k1.Hex
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.primal.core.networking.nwc.model.LightningPayRequest
import net.primal.core.networking.nwc.model.LightningPayResponse
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.networking.nwc.model.NwcWalletRequest
import net.primal.core.networking.nwc.model.PayInvoiceRequest
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.networking.sockets.filterBySubscriptionId
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.signOrThrow
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


    private val scope = CoroutineScope(DispatcherProviderFactory.create().io())

    private val nwcRelay by lazy {
        val relayUrl = nwcData.relays.first()
        NostrSocketClientFactory.create(wssUrl = "wss://nwc.primal.net/")
    }

    init {

    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    private suspend fun ensureSocketConnectionAndListeningEvents() {
        val id = Uuid.random().toPrimalSubscriptionId()
        nwcRelay.ensureSocketConnectionOrThrow()
        nwcRelay.sendREQ(
            subscriptionId = id,
            data = buildJsonObject {
//                put("kinds", buildJsonArray { add(23_195) })
                put(
                    "#p",
                    buildJsonArray {
                        add(nwcData.pubkey)
                        add(nwcData.keypair.pubkey)
                    },
                )
                put("until", Clock.System.now().epochSeconds)
            },
        )

        scope.launch {
            nwcRelay.incomingMessages
                .filterBySubscriptionId(id = id)
                .collect()
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
                nostrEvent = signWalletInvoiceRequestNostrEvent(
                    request = invoice.toWalletPayRequest(),
                    nwc = nwcData,
                ).unwrapOrThrow(),
            )
        } catch (error: NostrPublishException) {
            throw ZapFailureException(cause = error)
        } catch (error: SignatureException) {
            throw ZapFailureException(cause = error)
        }
    }

    private suspend fun NwcZapHelper.fetchZapPayRequestOrThrow(lnUrl: String): LightningPayRequest {
        return try {
            fetchZapPayRequest(lnUrl)
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        } catch (error: IllegalArgumentException) {
            throw ZapFailureException(cause = error)
        }
    }

    private suspend fun NwcZapHelper.fetchInvoiceOrThrow(
        zapPayRequest: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmountInMilliSats: ULong,
        comment: String = "",
    ): LightningPayResponse {
        val fetchInvoiceResult = runCatching {
            this.fetchInvoice(
                request = zapPayRequest,
                zapEvent = zapEvent,
                satoshiAmountInMilliSats = satoshiAmountInMilliSats,
                comment = comment,
            )
        }

        return fetchInvoiceResult.getOrNull()
            ?: throw ZapFailureException(cause = fetchInvoiceResult.exceptionOrNull())
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun signWalletInvoiceRequestNostrEvent(
        request: NwcWalletRequest<PayInvoiceRequest>,
        nwc: NostrWalletConnect,
    ): SignResult {
        val tags = listOf(nwc.pubkey.asPubkeyTag())
        val content = CommonJson.encodeToString(request)
        val encryptedMessage = CryptoUtils.encrypt(
            msg = content,
            privateKey = Hex.decode(nwc.keypair.privateKey),
            pubKey = Hex.decode(nwc.pubkey),
        )

        return runCatching {
            SignResult.Signed(
                NostrUnsignedEvent(
                    pubKey = nwc.keypair.pubkey,
                    kind = NostrEventKind.WalletRequest.value,
                    content = encryptedMessage,
                    tags = tags,
                ).signOrThrow(hexPrivateKey = Hex.decode(nwc.keypair.privateKey)),
            )
        }.getOrElse { SignResult.Rejected(SignatureException(cause = it)) }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getBalance(): Long {
        val id = Uuid.random().toPrimalSubscriptionId()

        ensureSocketConnectionAndListeningEvents()
        nwcRelay.sendREQ(
            subscriptionId = id,
            data = signWalletBalanceRequestNostrEvent(
                request = NwcWalletRequest<JsonObject>(
                    method = "get_balance",
                    params = buildJsonObject {
                        put("balance", true)
                    },
                ),
                nwc = nwcData,
            ).unwrapOrThrow().toNostrJsonObject(),
        )

        return 0L
    }

    override suspend fun getTransactions(): List<JsonObject> {
        return emptyList()
    }

    override suspend fun publishNostrEvent(nostrEvent: NostrEvent, outboxRelays: List<String>) {
        nwcRelay.ensureSocketConnectionOrThrow()
        nwcRelay.sendEVENT(nostrEvent.toNostrJsonObject())
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun signWalletBalanceRequestNostrEvent(request: NwcWalletRequest<JsonObject>, nwc: NostrWalletConnect): SignResult {
        val tags = listOf(nwc.pubkey.asPubkeyTag())

        val plaintext = CommonJson.encodeToString(request.params)

        val encrypted = CryptoUtils.encrypt(
            msg = plaintext,
            privateKey = Hex.decode(nwc.keypair.privateKey),
            pubKey = Hex.decode(nwc.pubkey),
        )

        val unsigned = NostrUnsignedEvent(
            pubKey = nwc.keypair.pubkey,
            kind = NostrEventKind.WalletRequest.value,
            content = encrypted,
            tags = tags,
        )

        return runCatching {
            SignResult.Signed(
                unsigned.signOrThrow(hexPrivateKey = Hex.decode(nwc.keypair.privateKey)),
            )
        }.getOrElse { SignResult.Rejected(SignatureException(cause = it)) }
    }
}
