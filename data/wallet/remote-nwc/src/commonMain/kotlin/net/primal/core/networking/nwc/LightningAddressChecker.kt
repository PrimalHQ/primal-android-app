package net.primal.core.networking.nwc

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.utils.parseAsLNUrlOrNull

class LightningAddressChecker(
    private val dispatcherProvider: DispatcherProvider,
    private val httpClient: HttpClient = NwcClientFactory.nwcHttpClient,
) {

    private fun String.parseAsLnUrlOrThrow(): String {
        val lnUrl = this.parseAsLNUrlOrNull()
        return if (lnUrl.isNullOrEmpty()) throw InvalidLud16Exception(lud16 = this) else lnUrl
    }

    suspend fun validateLightningAddress(lud16: String) {
        val lnUrl = lud16.parseAsLnUrlOrThrow()

        try {
            val response = withContext(dispatcherProvider.io()) {
                httpClient.get(lnUrl)
            }

            if (response.status.value != 200) {
                throw InvalidLud16Exception(lud16 = lud16)
            }
        } catch (error: IOException) {
            throw InvalidLud16Exception(cause = error, lud16 = lud16)
        } catch (error: Throwable) {
            throw InvalidLud16Exception(cause = error, lud16 = lud16)
        }
    }
}

class InvalidLud16Exception(
    override val cause: Throwable? = null,
    val lud16: String,
) : RuntimeException("Invalid LUD-16 address: $lud16", cause)
