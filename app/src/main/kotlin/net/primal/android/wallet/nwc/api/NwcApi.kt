package net.primal.android.wallet.nwc.api

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.wallet.nwc.model.LightningPayRequest
import net.primal.android.wallet.nwc.model.LightningPayResponse
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.nostr.utils.LnInvoiceUtils
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

@Singleton
class NwcApi @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val okHttpClient: OkHttpClient,
) {
    suspend fun fetchZapPayRequest(lnUrl: String): LightningPayRequest {
        val getRequest = Request.Builder()
            .header("Content-Type", "application/json")
            .url(lnUrl)
            .get()
            .build()

        val response = withContext(dispatcherProvider.io()) {
            okHttpClient.newCall(getRequest).execute()
        }

        val responseBody = response.body
        return if (responseBody != null) {
            val responseString = withContext(dispatcherProvider.io()) { responseBody.string() }
            CommonJson.decodeFromStringOrNull(string = responseString)
                ?: throw IOException("Invalid body content.")
        } else {
            throw IOException("Empty response body.")
        }
    }

    suspend fun fetchInvoice(
        request: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmountInMilliSats: ULong,
        comment: String = "",
    ): LightningPayResponse {
        require(request.allowsNostr == true)

        val zapEventString = zapEvent.toNostrJsonObject().encodeToJsonString()

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

        val response = withContext(dispatcherProvider.io()) { okHttpClient.newCall(getRequest).execute() }
        val responseString = withContext(dispatcherProvider.io()) { response.body?.string() }
        val decoded = CommonJson.decodeFromStringOrNull<LightningPayResponse>(responseString)

        val responseInvoiceAmountInMillis = decoded?.pr?.extractInvoiceAmountInMilliSats()
        val requestAmountInMillis = satoshiAmountInMilliSats.toLong().toBigDecimal().longValue()

        if (decoded == null || requestAmountInMillis != responseInvoiceAmountInMillis) {
            throw IOException("Invalid invoice response.")
        }

        return decoded
    }

    private val thousandAsBigDecimal = BigDecimal.fromInt(1_000)

    private fun String.extractInvoiceAmountInMilliSats(): Long? {
        return try {
            LnInvoiceUtils.getAmountInSats(this)
                .multiply(thousandAsBigDecimal)
                .longValue()
        } catch (error: LnInvoiceUtils.AddressFormatException) {
            Timber.w(error)
            null
        }
    }
}
