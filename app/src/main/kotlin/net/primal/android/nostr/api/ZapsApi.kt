package net.primal.android.nostr.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.primal.android.crypto.Bech32
import net.primal.android.nostr.ext.toLightningUrlOrNull
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.zap.LightningPayRequest
import net.primal.android.nostr.model.zap.LightningPayResponse
import net.primal.android.nostr.model.zap.ZapTarget
import net.primal.android.serialization.toJsonObject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.http.Url
import java.security.InvalidParameterException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZapsApi @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchPayRequest(lightningAddress: String): LightningPayRequest? {
        val lnUrl = lightningAddress.toLightningUrlOrNull() ?: throw InvalidParameterException()

        val getRequest = Request.Builder()
            .header("Content-Type", "application/json")
            .url(lnUrl)
            .get()
            .build()

        val result = withContext(Dispatchers.IO) { okHttpClient.newCall(getRequest).execute() }
        if (result.body != null) {
            val res = result.body!!.string()
            return json.decodeFromString(res)
        }

        return null
    }

    suspend fun fetchInvoice(
        request: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmount: Int,
        comment: String = ""
    ): LightningPayResponse? {
        if (request.allowsNostr != null && request.allowsNostr == false) return null
        if (request.callback == null) return null

        val zapEventString = json.encodeToString(zapEvent.toJsonObject())

        val builder = request
            .callback
            .toHttpUrl()
            .newBuilder()

        builder.addQueryParameter("nostr", zapEventString.replace("\\\\",""))
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
            return json.decodeFromString(res)
        }

        return null
    }

}