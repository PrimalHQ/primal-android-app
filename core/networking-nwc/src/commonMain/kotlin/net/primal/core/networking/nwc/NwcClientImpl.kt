package net.primal.core.networking.nwc

import fr.acinq.secp256k1.Hex
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.io.IOException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject
import net.primal.core.networking.nwc.model.LightningPayRequest
import net.primal.core.networking.nwc.model.LightningPayResponse
import net.primal.core.networking.nwc.model.NostrWalletConnect
import net.primal.core.networking.nwc.model.NwcWalletRequest
import net.primal.core.networking.nwc.model.PayInvoiceRequest
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
import net.primal.domain.nostr.zaps.NostrZapper
import net.primal.domain.nostr.zaps.ZapFailureException
import net.primal.domain.nostr.zaps.ZapRequestData

internal class NwcClientImpl(
    private val nwcData: NostrWalletConnect,
    private val nwcZapHelper: NwcZapHelper,
) : NostrZapper, NwcApi, NostrEventPublisher {


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

    override suspend fun getBalance(): Long {
        return 0L
    }

    override suspend fun getTransactions(): List<JsonObject> {
        return emptyList()
    }

    override suspend fun publishNostrEvent(nostrEvent: NostrEvent, outboxRelays: List<String>) {

    }

    @OptIn(ExperimentalEncodingApi::class)
    fun signWalletBalanceRequestNostrEvent(request: NwcWalletRequest<Unit>, nwc: NostrWalletConnect): SignResult {
        val tags = listOf(nwc.pubkey.asPubkeyTag())

        val plaintext = CommonJson.encodeToString(
            NwcWalletRequest.serializer(Unit.serializer()),
            request,
        )

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
