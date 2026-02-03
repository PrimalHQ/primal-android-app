package net.primal.core.networking.nwc.wallet

import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.core.networking.nwc.nip47.ListTransactionsParams
import net.primal.core.networking.nwc.nip47.LookupInvoiceParams
import net.primal.core.networking.nwc.nip47.MakeInvoiceParams
import net.primal.core.networking.nwc.nip47.NwcEncryptionScheme
import net.primal.core.networking.nwc.nip47.NwcMethod
import net.primal.core.networking.nwc.nip47.PayInvoiceParams
import net.primal.core.networking.nwc.nip47.PayKeysendParams
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.Result
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstEncryptionTag

class NwcWalletRequestParser(
    private val encryptionService: NostrEncryptionService,
) {

    fun parseNostrEvent(event: NostrEvent, connection: NwcConnection): Result<WalletNwcRequest> {
        return runCatching {
            val encryptionTag = event.tags.findFirstEncryptionTag()
            val encryptionScheme = NwcEncryptionScheme.fromValueOrDefault(encryptionTag)

            val decryptedContent = when (encryptionScheme) {
                NwcEncryptionScheme.NIP44 -> encryptionService.nip44Decrypt(
                    privateKey = connection.serviceKeyPair.privateKey,
                    pubKey = event.pubKey,
                    ciphertext = event.content,
                ).getOrThrow()
                NwcEncryptionScheme.NIP04 -> encryptionService.nip04Decrypt(
                    privateKey = connection.serviceKeyPair.privateKey,
                    pubKey = event.pubKey,
                    ciphertext = event.content,
                ).getOrThrow()
            }

            val request = decryptedContent.decodeFromJsonStringOrNull<NwcWalletRequestRaw>()
                ?: return Result.failure(IllegalArgumentException("Failed to parse NWC JSON."))

            val walletRequest = parseMethod(
                method = request.method,
                params = request.params,
                event = event,
                connection = connection,
                encryptionScheme = encryptionScheme,
            )

            walletRequest ?: throw IllegalArgumentException("Unsupported NWC method: ${request.method}")
        }.onFailure {
            Napier.w("NwcWalletRequestParser failed to parse event ${event.id}", it)
        }
    }

    private fun parseMethod(
        method: String,
        params: JsonElement?,
        event: NostrEvent,
        connection: NwcConnection,
        encryptionScheme: NwcEncryptionScheme,
    ): WalletNwcRequest? {
        return when (method) {
            NwcMethod.PayInvoice.value -> {
                params ?: return null
                WalletNwcRequest.PayInvoice(
                    eventId = event.id,
                    connection = connection,
                    encryptionScheme = encryptionScheme,
                    params = CommonJson.decodeFromJsonElement<PayInvoiceParams>(params),
                )
            }

            NwcMethod.PayKeysend.value -> {
                params ?: return null
                WalletNwcRequest.PayKeysend(
                    eventId = event.id,
                    connection = connection,
                    encryptionScheme = encryptionScheme,
                    params = CommonJson.decodeFromJsonElement<PayKeysendParams>(params),
                )
            }

            NwcMethod.MakeInvoice.value -> {
                params ?: return null
                WalletNwcRequest.MakeInvoice(
                    eventId = event.id,
                    connection = connection,
                    encryptionScheme = encryptionScheme,
                    params = CommonJson.decodeFromJsonElement<MakeInvoiceParams>(params),
                )
            }

            NwcMethod.LookupInvoice.value -> {
                params ?: return null
                WalletNwcRequest.LookupInvoice(
                    eventId = event.id,
                    connection = connection,
                    encryptionScheme = encryptionScheme,
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
                    encryptionScheme = encryptionScheme,
                    params = listParams,
                )
            }

            NwcMethod.GetBalance.value -> {
                WalletNwcRequest.GetBalance(
                    eventId = event.id,
                    connection = connection,
                    encryptionScheme = encryptionScheme,
                )
            }

            NwcMethod.GetInfo.value -> {
                WalletNwcRequest.GetInfo(
                    eventId = event.id,
                    connection = connection,
                    encryptionScheme = encryptionScheme,
                )
            }

            NwcMethod.MultiPayInvoice.value -> {
                params ?: return null
                WalletNwcRequest.MultiPayInvoice(
                    eventId = event.id,
                    connection = connection,
                    encryptionScheme = encryptionScheme,
                    params = CommonJson.decodeFromJsonElement<List<PayInvoiceParams>>(params),
                )
            }

            NwcMethod.MultiPayKeysend.value -> {
                params ?: return null
                WalletNwcRequest.MultiPayKeysend(
                    eventId = event.id,
                    connection = connection,
                    encryptionScheme = encryptionScheme,
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
