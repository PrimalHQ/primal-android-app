package net.primal.core.lightning

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import net.primal.core.lightning.model.LightningPayRequest
import net.primal.core.lightning.model.LightningPayResponse
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.toLong
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.domain.nostr.utils.decodeLNUrlOrNull

private const val REQUEST_TIMEOUT_MS = 30_000L

class LightningPayHelper(
    private val dispatcherProvider: DispatcherProvider,
    private val httpClient: HttpClient = LightningHttpClient.defaultHttpClient,
) {
    suspend fun fetchPayRequest(lnUrl: String): LightningPayRequest {
        val decodedLnUrl = if (lnUrl.startsWith("http://") || lnUrl.startsWith("https://")) {
            lnUrl
        } else {
            lnUrl.decodeLNUrlOrNull()
        } ?: throw IllegalArgumentException("Couldn't decode '$lnUrl'.")

        val bodyString = withContext(dispatcherProvider.io()) {
            httpClient.get(decodedLnUrl) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MS }
            }.bodyAsText()
        }

        if (bodyString.isBlank()) {
            throw IOException("Empty response body.")
        }

        return bodyString.decodeFromJsonStringOrNull<LightningPayRequest>()
            ?: throw IOException("Invalid body content.")
    }

    suspend fun fetchInvoice(
        payRequest: LightningPayRequest,
        amountInMilliSats: ULong,
        comment: String = "",
        zapEvent: NostrEvent? = null,
    ): LightningPayResponse {
        if (zapEvent != null) require(payRequest.allowsNostr == true)

        val nostrQueryValue = zapEvent?.toNostrJsonObject()?.toString()

        val rawUrl = URLBuilder(payRequest.callback).apply {
            nostrQueryValue?.let { parameters.append("nostr", it) }
            parameters.append("amount", amountInMilliSats.toString())

            val maxComment = payRequest.commentAllowed
            if (maxComment > 0 && comment.isNotBlank()) {
                parameters.append("comment", comment.take(maxComment))
            }
        }.buildString()

        val bodyString = withContext(dispatcherProvider.io()) {
            httpClient.get(rawUrl) {
                timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MS }
            }.bodyAsText()
        }

        val decoded = bodyString.decodeFromJsonStringOrNull<LightningPayResponse>()
            ?: throw IOException("Invalid invoice response (bad JSON).\n$bodyString")

        val invoiceMilliSats = decoded.invoice.extractInvoiceAmountInMilliSats()
        val requestedMilliSats = amountInMilliSats.toLong()
        if (invoiceMilliSats != requestedMilliSats) {
            throw IOException("Invoice amount mismatch ($invoiceMilliSats ≠ $requestedMilliSats).")
        }

        return decoded
    }

    private val thousandAsBigDecimal = BigDecimal.fromInt(1_000)

    private fun String.extractInvoiceAmountInMilliSats(): Long? {
        return LnInvoiceUtils.getAmountInSatsOrNull(this)?.multiply(thousandAsBigDecimal)?.toLong()
    }
}
