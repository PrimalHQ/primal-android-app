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
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.regex.Pattern
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
        satoshiAmount: Int,
        comment: String = ""
    ): LightningPayResponse {
        if (request.allowsNostr != null && request.allowsNostr == false) {
            throw IllegalArgumentException("request.allowsNostr must not be null or false.")
        }

        val zapEventString = NostrJson.encodeToString(zapEvent.toJsonObject())

        val builder = request.callback.toHttpUrl().newBuilder()
        builder.addQueryParameter("nostr", zapEventString)
        builder.addQueryParameter("amount", satoshiAmount.toString())

        if (request.commentAllowed != null) {
            builder.addQueryParameter("comment", comment.take(request.commentAllowed))
        }

        val getRequest = Request.Builder()
            .url(builder.build())
            .get()
            .build()

        val response = withContext(Dispatchers.IO) { okHttpClient.newCall(getRequest).execute() }
        val responseBody = response.body
        return if (responseBody != null) {
            val decoded = NostrJson.decodeFromStringOrNull<LightningPayResponse>(responseBody.string())
            when {
                decoded?.pr == null -> throw IOException("Invalid invoice response.")
                getAmount(decoded.pr) != satoshiAmount -> throw IOException("Amount mismatch.")
                else -> decoded
            }
        } else {
            throw IOException("Empty response body.")
        }
    }

    private val invoicePattern = Pattern.compile(
        "lnbc((?<amount>\\d+)(?<multiplier>[munp])?)?1[^1\\s]+",
        Pattern.CASE_INSENSITIVE
    )

    private fun getAmount(invoice: String): Int {
        val matcher = invoicePattern.matcher(invoice)
        require(matcher.matches()) { "Failed to match HRP pattern" }
        val amountGroup = matcher.group("amount")
        val multiplierGroup = matcher.group("multiplier")
        if (amountGroup == null) {
            return 0
        }
        val amount = amountGroup.toInt()
        if (multiplierGroup == null) {
            return amount
        }
        require(!(multiplierGroup == "p" && amountGroup[amountGroup.length - 1] != '0')) { "sub-millisatoshi amount" }
        return amount * 100
    }
}
