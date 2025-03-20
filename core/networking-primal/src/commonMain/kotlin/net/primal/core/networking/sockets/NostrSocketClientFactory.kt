package net.primal.core.networking.sockets

import io.ktor.client.HttpClient
import net.primal.core.networking.factory.defaultSocketsHttpClient
import net.primal.core.utils.coroutines.DispatcherProviderFactory

object NostrSocketClientFactory {

    fun create(
        wssUrl: String,
        httpClient: HttpClient,
        incomingCompressionEnabled: Boolean = false,
        onSocketConnectionOpened: SocketConnectionOpenedCallback? = null,
        onSocketConnectionClosed: SocketConnectionClosedCallback? = null,
    ): NostrSocketClient {
        return NostrSocketClientImpl(
            dispatcherProvider = DispatcherProviderFactory.create(),
            httpClient = httpClient,
            wssUrl = wssUrl,
            incomingCompressionEnabled = incomingCompressionEnabled,
            onSocketConnectionOpened = onSocketConnectionOpened,
            onSocketConnectionClosed = onSocketConnectionClosed,
        )
    }

    fun create(
        wssUrl: String,
        incomingCompressionEnabled: Boolean = false,
        onSocketConnectionOpened: SocketConnectionOpenedCallback? = null,
        onSocketConnectionClosed: SocketConnectionClosedCallback? = null,
    ) = create(
        httpClient = defaultSocketsHttpClient,
        wssUrl = wssUrl,
        incomingCompressionEnabled = incomingCompressionEnabled,
        onSocketConnectionOpened = onSocketConnectionOpened,
        onSocketConnectionClosed = onSocketConnectionClosed,
    )
}
