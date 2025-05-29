package net.primal.core.networking.nwc

import io.ktor.client.HttpClient
import net.primal.core.networking.nwc.model.LightningPayRequest
import net.primal.core.networking.nwc.model.LightningPayResponse
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.NostrEvent

class NwcZapHelper(
    private val dispatcherProvider: DispatcherProvider,
    private val httpClient: HttpClient,
) {
    suspend fun fetchZapPayRequest(lnUrl: String): LightningPayRequest {
        // TODO Port this to KMP
        throw NotImplementedError()
//        val getRequest = Request.Builder()
//            .header("Content-Type", "application/json")
//            .url(lnUrl)
//            .get()
//            .build()
//
//        val response = withContext(dispatcherProvider.io()) {
//            httpClient.newCall(getRequest).execute()
//        }
//
//        val responseBody = response.body
//        return if (responseBody != null) {
//            val responseString = withContext(dispatcherProvider.io()) { responseBody.string() }
//            responseString.decodeFromJsonStringOrNull()
//
//                ?: throw IOException("Invalid body content.")
//        } else {
//            throw IOException("Empty response body.")
//        }
    }

    suspend fun fetchInvoice(
        request: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmountInMilliSats: ULong,
        comment: String = "",
    ): LightningPayResponse {
        require(request.allowsNostr == true)

        // TODO Port this to KMP
        throw NotImplementedError()

//        val zapEventString = zapEvent.toNostrJsonObject().encodeToJsonString()
//
//        val builder = request.callback.toHttpUrl().newBuilder()
//        builder.addQueryParameter("nostr", zapEventString)
//        builder.addQueryParameter("amount", satoshiAmountInMilliSats.toString())
//
//        val commentAllowedMaxChars = request.commentAllowed
//        if (commentAllowedMaxChars != null) {
//            builder.addQueryParameter("comment", comment.take(commentAllowedMaxChars))
//        }
//
//        val getRequest = Request.Builder()
//            .url(builder.build())
//            .get()
//            .build()
//
//        val response = withContext(dispatcherProvider.io()) { okHttpClient.newCall(getRequest).execute() }
//        val responseString = withContext(dispatcherProvider.io()) { response.body?.string() }
//        val decoded = responseString.decodeFromJsonStringOrNull<LightningPayResponse>()
//
//        val responseInvoiceAmountInMillis = decoded?.pr?.extractInvoiceAmountInMilliSats()
//        val requestAmountInMillis = satoshiAmountInMilliSats.toLong().toBigDecimal().toLong()
//
//        if (decoded == null || requestAmountInMillis != responseInvoiceAmountInMillis) {
//            throw IOException("Invalid invoice response.")
//        }
//
//        return decoded
    }

//    private val thousandAsBigDecimal = BigDecimal.fromInt(1_000)

//    private fun String.extractInvoiceAmountInMilliSats(): Long? {
//        return try {
//            LnInvoiceUtils.getAmountInSats(this)
//                .multiply(thousandAsBigDecimal)
//                .toLong()
//        } catch (error: LnInvoiceUtils.AddressFormatException) {
//            Timber.w(error)
//            null
//        }
//    }
}
