package net.primal.core.networking.nwc.wallet

import fr.acinq.secp256k1.Hex
import io.github.aakira.napier.Napier
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.core.networking.nwc.nip47.ListTransactionsParams
import net.primal.core.networking.nwc.nip47.LookupInvoiceParams
import net.primal.core.networking.nwc.nip47.MakeInvoiceParams
import net.primal.core.networking.nwc.nip47.NwcMethod
import net.primal.core.networking.nwc.nip47.PayInvoiceParams
import net.primal.core.networking.nwc.nip47.PayKeysendParams
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.utils.Result
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.utils.CryptoUtils

class NwcWalletRequestParser {

    @OptIn(ExperimentalEncodingApi::class)
    fun parseNostrEvent(event: NostrEvent, connection: NwcConnection): Result<WalletNwcRequest> {
        return try {
            val decryptedContent = CryptoUtils.decrypt(
                message = event.content,
                privateKey = Hex.decode(connection.serviceKeyPair.privateKey),
                pubKey = Hex.decode(event.pubKey),
            )

            val request = decryptedContent.decodeFromJsonStringOrNull<NwcWalletRequestRaw>()
                ?: return Result.failure(IllegalArgumentException("Failed to parse NWC JSON."))

            val walletRequest = parseMethod(
                method = request.method,
                params = request.params,
                event = event,
                connection = connection,
            )

            if (walletRequest != null) {
                Result.success(walletRequest)
            } else {
                Result.failure(IllegalArgumentException("Unsupported NWC method: ${request.method}"))
            }
        } catch (e: Exception) {
            Napier.w("NwcWalletRequestParser failed to parse event ${event.id}", e)
            Result.failure(e)
        }
    }

    private fun parseMethod(
        method: String,
        params: JsonElement?,
        event: NostrEvent,
        connection: NwcConnection,
    ): WalletNwcRequest? {
        return when (method) {
            NwcMethod.PayInvoice.value -> {
                params ?: return null
                WalletNwcRequest.PayInvoice(
                    eventId = event.id,
                    connection = connection,
                    params = CommonJson.decodeFromJsonElement<PayInvoiceParams>(params),
                )
            }
            NwcMethod.PayKeysend.value -> {
                params ?: return null
                WalletNwcRequest.PayKeysend(
                    eventId = event.id,
                    connection = connection,
                    params = CommonJson.decodeFromJsonElement<PayKeysendParams>(params),
                )
            }
            NwcMethod.MakeInvoice.value -> {
                params ?: return null
                WalletNwcRequest.MakeInvoice(
                    eventId = event.id,
                    connection = connection,
                    params = CommonJson.decodeFromJsonElement<MakeInvoiceParams>(params),
                )
            }
            NwcMethod.LookupInvoice.value -> {
                params ?: return null
                WalletNwcRequest.LookupInvoice(
                    eventId = event.id,
                    connection = connection,
                    params = CommonJson.decodeFromJsonElement<LookupInvoiceParams>(params),
                )
            }
            NwcMethod.ListTransactions.value -> {
                val listParams = if (params != null) {
                    CommonJson.decodeFromJsonElement<ListTransactionsParams>(params)
                } else {
                    ListTransactionsParams()
                }
                WalletNwcRequest.ListTransactions(
                    eventId = event.id,
                    connection = connection,
                    params = listParams,
                )
            }
            NwcMethod.GetBalance.value -> {
                WalletNwcRequest.GetBalance(
                    eventId = event.id,
                    connection = connection,
                )
            }
            NwcMethod.GetInfo.value -> {
                WalletNwcRequest.GetInfo(
                    eventId = event.id,
                    connection = connection,
                )
            }
            NwcMethod.MultiPayInvoice.value -> {
                params ?: return null
                WalletNwcRequest.MultiPayInvoice(
                    eventId = event.id,
                    connection = connection,
                    params = CommonJson.decodeFromJsonElement<List<PayInvoiceParams>>(params),
                )
            }
            NwcMethod.MultiPayKeysend.value -> {
                params ?: return null
                WalletNwcRequest.MultiPayKeysend(
                    eventId = event.id,
                    connection = connection,
                    params = CommonJson.decodeFromJsonElement<List<PayKeysendParams>>(params),
                )
            }
            else -> null
        }
    }

    @Serializable
    private data class NwcWalletRequestRaw(
        val method: String,
        val params: JsonElement? = null,
    )
}
