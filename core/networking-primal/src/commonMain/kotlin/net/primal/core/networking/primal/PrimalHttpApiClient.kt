package net.primal.core.networking.primal

import kotlinx.serialization.json.JsonElement
import net.primal.domain.common.exception.NetworkException

/**
 * HTTP counterpart of [PrimalApiClient]. Takes the same [PrimalCacheFilter] request envelope, but
 * sends it as a single request-response exchange instead of a socket subscription.
 */
interface PrimalHttpApiClient {

    @Throws(NetworkException::class, kotlin.coroutines.cancellation.CancellationException::class)
    suspend fun query(message: PrimalCacheFilter): JsonElement
}
