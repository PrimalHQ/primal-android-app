package net.primal.core.networking.sockets

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.JsonObject
import net.primal.domain.common.exception.NetworkException

interface NostrSocketClient {
    val socketUrl: String

    val incomingMessages: SharedFlow<NostrIncomingMessage>

    suspend fun close()

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun ensureSocketConnectionOrThrow()

    suspend fun sendAUTH(signedEvent: JsonObject)

    suspend fun sendCLOSE(subscriptionId: String)

    suspend fun sendCOUNT(data: JsonObject): String

    suspend fun sendEVENT(signedEvent: JsonObject)

    suspend fun sendREQ(subscriptionId: String, data: JsonObject)
}
