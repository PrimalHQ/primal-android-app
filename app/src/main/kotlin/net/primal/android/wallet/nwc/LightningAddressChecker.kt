package net.primal.android.wallet.nwc

import java.io.IOException
import java.net.HttpURLConnection
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.wallet.api.parseAsLNUrlOrNull
import net.primal.core.utils.coroutines.DispatcherProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly

class LightningAddressChecker @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val okHttpClient: OkHttpClient,
) {

    private fun String.parseAsLnUrlOrThrow(): String {
        val lnUrl = this.parseAsLNUrlOrNull()
        return if (lnUrl.isNullOrEmpty()) throw InvalidLud16Exception(lud16 = this) else lnUrl
    }

    suspend fun validateLightningAddress(lud16: String) {
        val lnUrl = lud16.parseAsLnUrlOrThrow()
        val lnUrlCall = okHttpClient.newCall(Request.Builder().url(lnUrl).build())
        try {
            val lnUrlResponse = withContext(dispatcherProvider.io()) { lnUrlCall.execute() }
            lnUrlResponse.closeQuietly()
            if (lnUrlResponse.code != HttpURLConnection.HTTP_OK) {
                throw InvalidLud16Exception(lud16 = lud16)
            }
        } catch (error: IOException) {
            throw InvalidLud16Exception(cause = error, lud16 = lud16)
        }
    }
}

class InvalidLud16Exception(
    override val cause: Throwable? = null,
    val lud16: String,
) : RuntimeException()
