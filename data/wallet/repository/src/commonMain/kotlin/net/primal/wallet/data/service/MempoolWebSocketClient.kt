package net.primal.wallet.data.service

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import net.primal.core.networking.factory.HttpClientFactory

internal class MempoolWebSocketClient(
    private val httpClient: HttpClient = HttpClientFactory.createHttpClientWithDefaultConfig {
        install(WebSockets)
    },
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun observeAddress(address: String): Flow<MempoolAddressEvent> =
        flow {
            val session = httpClient.webSocketSession(urlString = WSS_URL)
            try {
                session.send(Frame.Text("""{"track-address":"$address"}"""))

                for (frame in session.incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        parseMempoolEvent(text)?.let { emit(it) }
                    }
                }
            } finally {
                session.close()
            }
        }

    private fun parseMempoolEvent(text: String): MempoolAddressEvent? {
        val obj = runCatching { json.parseToJsonElement(text) as JsonObject }.getOrNull() ?: return null

        val addressTxs = obj["address-transactions"]
        if (addressTxs != null) {
            val transactions = json.decodeFromJsonElement(
                kotlinx.serialization.builtins.ListSerializer(MempoolTransaction.serializer()),
                addressTxs.jsonArray,
            )
            return MempoolAddressEvent.MempoolTx(transactions)
        }

        val blockTxs = obj["block-transactions"]
        if (blockTxs != null) {
            val transactions = json.decodeFromJsonElement(
                kotlinx.serialization.builtins.ListSerializer(MempoolTransaction.serializer()),
                blockTxs.jsonArray,
            )
            return MempoolAddressEvent.ConfirmedTx(transactions)
        }

        return null
    }

    private companion object {
        private const val WSS_URL = "wss://mempool.space/api/v1/ws"
    }
}

internal sealed interface MempoolAddressEvent {
    data class MempoolTx(val transactions: List<MempoolTransaction>) : MempoolAddressEvent
    data class ConfirmedTx(val transactions: List<MempoolTransaction>) : MempoolAddressEvent
}
