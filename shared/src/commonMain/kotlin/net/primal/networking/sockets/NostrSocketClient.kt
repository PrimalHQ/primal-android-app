package net.primal.networking.sockets

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.JsonObject

interface NostrSocketClient {
    val socketUrl: String
    val incomingMessages: SharedFlow<NostrIncomingMessage>
    suspend fun close()
    suspend fun ensureSocketConnection()
    suspend fun sendAUTH(signedEvent: JsonObject)
    suspend fun sendCLOSE(subscriptionId: String)
    suspend fun sendCOUNT(data: JsonObject) : String
    suspend fun sendEVENT(signedEvent: JsonObject)
    suspend fun sendREQ(subscriptionId: String, data: JsonObject)
}
