package net.primal.android.nostr.api

import kotlinx.serialization.json.Json
import net.primal.android.crypto.Bech32
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.zap.LightningPayRequest
import net.primal.android.nostr.model.zap.LightningPayResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.InvalidParameterException
import javax.inject.Inject

class ZapsApiImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
): ZapsApi {
    override suspend fun fetchPayRequest(lightningUrl: String): LightningPayRequest? {
        val decoded = Bech32.decodeBytes(lightningUrl)

        if (decoded.first !== "lnurl") {
            throw InvalidParameterException()
        }

        val url = decoded.second.toString(Charsets.UTF_8)

        val getRequest = Request.Builder()
            .header("Content-Type", "application/json")
            .url(url)
            .get()
            .build()

        val result = okHttpClient.newCall(getRequest).execute()
        if (result.body != null) {
            val res = result.body!!.string()
            return Json.decodeFromString(res)
        }

        return null
    }

    override suspend fun fetchInvoice(
        request: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmount: ULong,
        comment: String
    ): LightningPayResponse? {
        TODO("Not yet implemented")
    }

}