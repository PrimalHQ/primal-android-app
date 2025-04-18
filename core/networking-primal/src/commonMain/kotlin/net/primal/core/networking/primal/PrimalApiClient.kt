package net.primal.core.networking.primal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.primal.core.networking.sockets.NostrIncomingMessage
import net.primal.core.networking.sockets.errors.WssException

interface PrimalApiClient {

    val connectionStatus: StateFlow<PrimalServerConnectionStatus>

    @Throws(WssException::class, kotlin.coroutines.cancellation.CancellationException::class)
    suspend fun query(message: PrimalCacheFilter): PrimalQueryResult

    suspend fun subscribe(subscriptionId: String, message: PrimalCacheFilter): Flow<NostrIncomingMessage>

    suspend fun closeSubscription(subscriptionId: String): Boolean
}
