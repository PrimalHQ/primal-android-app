package net.primal.core.networking.primal

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonElement
import net.primal.core.config.AppConfigProvider
import net.primal.core.config.observeApiUrlByType
import net.primal.core.utils.Result
import net.primal.core.utils.Result.Companion.failure
import net.primal.core.utils.getOrElse
import net.primal.core.utils.runCatching
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.common.exception.QueryTimeoutException
import net.primal.domain.global.PrimalServerType

/**
 * Short by design: the server keeps processing a request after we stop reading it, so waiting
 * longer buys nothing. Enforced by [BasePrimalHttpApiClient.query] rather than only by the
 * `HttpTimeout` plugin, so the deadline cannot drift from the timeout reported by
 * [QueryTimeoutException].
 */
internal val HTTP_API_QUERY_TIMEOUT = 10.seconds

internal class BasePrimalHttpApiClient(
    private val serverType: PrimalServerType,
    private val appConfigProvider: AppConfigProvider,
    private val primalHttpApi: PrimalHttpApi,
) : PrimalHttpApiClient {

    private val apiUrlMutex = Mutex()
    private var apiUrlFlow: StateFlow<String>? = null

    override suspend fun query(message: PrimalCacheFilter): JsonElement {
        val apiUrl = resolveApiUrl()

        return runCatchingRequest {
            withTimeout(HTTP_API_QUERY_TIMEOUT) {
                primalHttpApi.request(url = apiUrl, body = message.toPrimalJsonArray())
            }
        }.getOrElse { error ->
            throw error.asNetworkException(verb = message.primalVerb)
        }
    }

    /**
     * [observeApiUrlByType] is backed by `stateIn`, so resolving it per request would leave behind
     * a sharing coroutine and a subscriber each time. Resolved once, then only its value is read.
     */
    private suspend fun resolveApiUrl(): String {
        val urlFlow = apiUrlMutex.withLock {
            apiUrlFlow ?: appConfigProvider.observeApiUrlByType(type = serverType).also { apiUrlFlow = it }
        }
        return urlFlow.value.asPrimalHttpApiUrl()
    }

    /**
     * Timeouts surface as [CancellationException], which [runCatching] rethrows to keep cooperative
     * cancellation intact. Capture those, and let genuine caller cancellation propagate.
     */
    private inline fun <R> runCatchingRequest(block: () -> R): Result<R> =
        try {
            runCatching(block)
        } catch (error: CancellationException) {
            if (error.isRequestTimeout()) failure(error) else throw error
        }

    private fun Throwable.asNetworkException(verb: String?): NetworkException =
        when {
            isRequestTimeout() -> QueryTimeoutException(verb = verb, timeout = HTTP_API_QUERY_TIMEOUT, cause = this)
            else -> NetworkException(message = "$message [$verb]", cause = this)
        }

    /**
     * Ktor enforces `requestTimeoutMillis` by cancelling the request job with
     * [HttpRequestTimeoutException] as the cause rather than by throwing it.
     */
    private fun Throwable.isRequestTimeout(): Boolean =
        when (this) {
            is TimeoutCancellationException,
            is HttpRequestTimeoutException,
            is SocketTimeoutException,
            is ConnectTimeoutException,
            -> true

            else -> cause is HttpRequestTimeoutException
        }
}
