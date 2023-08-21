package net.primal.android.nostr.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import net.primal.android.nostr.ext.toLightningUrlOrNull
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.zap.LightningPayRequest
import net.primal.android.nostr.model.zap.LightningPayResponse
import net.primal.android.nostr.model.zap.PayInvoiceRequest
import net.primal.android.nostr.model.zap.WalletRequest
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull
import net.primal.android.serialization.toJsonObject
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
    suspend fun fetchZapPayRequest(lightningAddress: String): LightningPayRequest? {
        val lnUrl =
            lightningAddress.toLightningUrlOrNull() ?: throw MalformedLightningAddressException()

        val getRequest = Request.Builder()
            .header("Content-Type", "application/json")
            .url(lnUrl)
            .get()
            .build()

        val result = withContext(Dispatchers.IO) {
            try {
                okHttpClient.newCall(getRequest).execute()
            } catch (error: IOException) {
                null
            }
        }

        val responseBody = result?.body
        return if (responseBody != null) {
            NostrJson.decodeFromStringOrNull(string = responseBody.string())
        } else {
            null
        }
    }

    suspend fun fetchInvoice(
        request: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmount: Int,
        comment: String = ""
    ): LightningPayResponse? {
        if (request.allowsNostr != null && request.allowsNostr == false) return null
        if (request.callback == null) return null

        val zapEventString = NostrJson.encodeToString(zapEvent.toJsonObject())

        val builder = request
            .callback
            .toHttpUrl()
            .newBuilder()

        builder.addQueryParameter("nostr", zapEventString)
        builder.addQueryParameter("amount", satoshiAmount.toString())

        if (request.commentAllowed != null) {
            builder.addQueryParameter("comment", comment.take(request.commentAllowed))
        }

        val getRequest = Request.Builder()
            .url(builder.build())
            .get()
            .build()

        val result = withContext(Dispatchers.IO) { okHttpClient.newCall(getRequest).execute() }
        if (result.body != null) {
            val res = result.body!!.string()
            val decoded = NostrJson.decodeFromStringOrNull<LightningPayResponse>(res)
            return when {
                decoded?.pr == null -> null
                getAmount(decoded.pr) != satoshiAmount -> null
                else -> decoded
            }
        }

        return null
    }

    fun createWalletPayRequest(
        invoice: LightningPayResponse
    ): WalletRequest<PayInvoiceRequest> =
        WalletRequest(method = "pay_invoice", params = PayInvoiceRequest(invoice.pr))

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

class MalformedLightningAddressException : IllegalArgumentException()
