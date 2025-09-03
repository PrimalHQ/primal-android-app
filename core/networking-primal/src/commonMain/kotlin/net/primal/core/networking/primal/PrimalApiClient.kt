package net.primal.core.networking.primal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.domain.common.exception.NetworkException

interface PrimalApiClient {

    val connectionStatus: StateFlow<PrimalServerConnectionStatus>

    @Throws(NetworkException::class, kotlin.coroutines.cancellation.CancellationException::class)
    suspend fun query(message: PrimalCacheFilter): PrimalQueryResult

    @Throws(NetworkException::class, kotlin.coroutines.cancellation.CancellationException::class)
    suspend fun subscribe(subscriptionId: String, message: PrimalCacheFilter): Flow<NostrIncomingMessage>

    @Throws(NetworkException::class, kotlin.coroutines.cancellation.CancellationException::class)
    suspend fun subscribeBuffered(
        subscriptionId: String,
        message: PrimalCacheFilter,
    ): Flow<PrimalSubscriptionBufferedResult>

    suspend fun closeSubscription(subscriptionId: String): Boolean
}
