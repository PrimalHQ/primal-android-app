package net.primal.core.networking.nwc

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import net.primal.core.networking.nwc.model.LightningPayRequest
import net.primal.core.networking.nwc.model.LightningPayResponse
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.core.utils.toLong
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.utils.LnInvoiceUtils

class NwcZapHelper(
    private val dispatcherProvider: DispatcherProvider,
    private val httpClient: HttpClient,
) {
    suspend fun fetchZapPayRequest(lnUrl: String): LightningPayRequest {
        val bodyString = withContext(dispatcherProvider.io()) {
            httpClient.get(lnUrl) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }.bodyAsText()
        }

        if (bodyString.isBlank()) {
            throw IOException("Empty response body.")
        }

        return bodyString.decodeFromJsonStringOrNull<LightningPayRequest>()
            ?: throw IOException("Invalid body content.")
    }

    suspend fun fetchInvoice(
        request: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmountInMilliSats: ULong,
        comment: String = "",
    ): LightningPayResponse {
        require(request.allowsNostr == true)
        val nostrJson = zapEvent.toNostrJsonObject().encodeToJsonString()

        val rawUrl = URLBuilder(request.callback).apply {
            parameters.append("nostr", nostrJson)
            parameters.append("amount", satoshiAmountInMilliSats.toString())

            val maxComment = request.commentAllowed
            if (maxComment != null && comment.isNotBlank()) {
                parameters.append("comment", comment.take(maxComment))
            }
        }.buildString()

        val bodyString = withContext(dispatcherProvider.io()) {
            httpClient.get(rawUrl).bodyAsText()
        }

        val decoded = bodyString.decodeFromJsonStringOrNull<LightningPayResponse>()
            ?: throw IOException("Invalid invoice response (bad JSON).")

        val invoiceMsat = decoded.pr.extractInvoiceAmountInMilliSats()
        val requestedMsat = satoshiAmountInMilliSats.toLong()
        if (invoiceMsat != requestedMsat) {
            throw IOException("Invoice amount mismatch ($invoiceMsat ≠ $requestedMsat).")
        }

        return decoded
    }

    private val thousandAsBigDecimal = BigDecimal.fromInt(1_000)

    private fun String.extractInvoiceAmountInMilliSats(): Long? {
        return try {
            LnInvoiceUtils.getAmountInSats(this)
                .multiply(thousandAsBigDecimal)
                .toLong()
        } catch (error: LnInvoiceUtils.AddressFormatException) {
            Napier.w(error) { "Cannot parse amount from LN invoice: \"$this\" — ${error.message}" }
            null
        }
    }
}
