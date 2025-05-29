package net.primal.core.networking.nwc

import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.utils.parseAsLNUrlOrNull

class LightningAddressChecker(
    private val dispatcherProvider: DispatcherProvider,
) {

    private fun String.parseAsLnUrlOrThrow(): String {
        val lnUrl = this.parseAsLNUrlOrNull()
        return if (lnUrl.isNullOrEmpty()) throw InvalidLud16Exception(lud16 = this) else lnUrl
    }

    suspend fun validateLightningAddress(lud16: String) {
        // TODO Port this to KMP
        throw NotImplementedError()

//        val lnUrl = lud16.parseAsLnUrlOrThrow()
//        val lnUrlCall = okHttpClient.newCall(Request.Builder().url(lnUrl).build())
//        try {
//            val lnUrlResponse = withContext(dispatcherProvider.io()) { lnUrlCall.execute() }
//            lnUrlResponse.closeQuietly()
//            if (lnUrlResponse.code != HttpURLConnection.HTTP_OK) {
//                throw InvalidLud16Exception(lud16 = lud16)
//            }
//        } catch (error: IOException) {
//            throw InvalidLud16Exception(cause = error, lud16 = lud16)
//        }
    }
}

class InvalidLud16Exception(
    override val cause: Throwable? = null,
    val lud16: String,
) : RuntimeException()
