package net.primal.networking.sockets

import net.primal.PrimalLib
import net.primal.networking.di.WebSocketHttpClient

object NostrSocketClientFactory {

    fun create(
        wssUrl: String,
        incomingCompressionEnabled: Boolean = false,
        onSocketConnectionOpened: SocketConnectionOpenedCallback? = null,
        onSocketConnectionClosed: SocketConnectionClosedCallback? = null,
    ): NostrSocketClient {
        val koin = PrimalLib.getKoin()
        return NostrSocketClientImpl(
            dispatcherProvider = koin.get(),
            httpClient = koin.get(WebSocketHttpClient),
            wssUrl = wssUrl,
            incomingCompressionEnabled = incomingCompressionEnabled,
            onSocketConnectionOpened = onSocketConnectionOpened,
            onSocketConnectionClosed = onSocketConnectionClosed,
        )
    }

}
