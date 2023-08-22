package net.primal.android.wallet.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull
import net.primal.android.serialization.toJsonObject
import net.primal.android.wallet.model.LightningPayRequest
import net.primal.android.wallet.model.LightningPayResponse
import net.primal.android.wallet.utils.LnInvoiceUtil
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZapsApi @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun fetchZapPayRequest(lightningAddress: String): LightningPayRequest {
        val lnUrl = lightningAddress.toLightningUrlOrNull()
            ?: throw IllegalArgumentException("Invalid lightningAddress.")

        val getRequest = Request.Builder()
            .header("Content-Type", "application/json")
            .url(lnUrl)
            .get()
            .build()

        val response = withContext(Dispatchers.IO) {
            okHttpClient.newCall(getRequest).execute()
        }

        val responseBody = response.body
        return if (responseBody != null) {
            NostrJson.decodeFromStringOrNull(string = responseBody.string())
                ?: throw IOException("Invalid body content.")
        } else {
            throw IOException("Empty response body.")
        }
    }

    suspend fun fetchInvoice(
        request: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmountInMilliSats: Int,
        comment: String = ""
    ): LightningPayResponse {
        if (request.allowsNostr != null && request.allowsNostr == false) {
            throw IllegalArgumentException("request.allowsNostr must not be null or false.")
        }

        val zapEventString = NostrJson.encodeToString(zapEvent.toJsonObject())

        val builder = request.callback.toHttpUrl().newBuilder()
        builder.addQueryParameter("nostr", zapEventString)
        builder.addQueryParameter("amount", satoshiAmountInMilliSats.toString())

        if (request.commentAllowed != null) {
            builder.addQueryParameter("comment", comment.take(request.commentAllowed))
        }

        val getRequest = Request.Builder()
            .url(builder.build())
            .get()
            .build()

        val response = withContext(Dispatchers.IO) { okHttpClient.newCall(getRequest).execute() }
        val responseBody = response.body
        if (responseBody != null) {
            val decoded = NostrJson.decodeFromStringOrNull<LightningPayResponse>(responseBody.string())
            if (decoded?.pr == null) throw IOException("Invalid invoice response.")

            val invoiceAmount = try {
                LnInvoiceUtil.getAmountInSats(decoded.pr)
            } catch (error: LnInvoiceUtil.AddressFormatException) {
                throw IOException("Invalid invoice response")
            }

            val amountInMillis = BigDecimal(satoshiAmountInMilliSats).toLong()
            val invoiceAmountInMillis = invoiceAmount.multiply(BigDecimal(1000)).toLong()
            if (amountInMillis != invoiceAmountInMillis) throw IOException("Amount mismatch.")

            return decoded
        } else {
            throw IOException("Empty response body.")
        }
    }
}
