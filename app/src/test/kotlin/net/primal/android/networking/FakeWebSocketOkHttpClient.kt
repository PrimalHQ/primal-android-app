package net.primal.android.networking

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class FakeWebSocketOkHttpClient(
    private val webSocket: FakeWebSocket = FakeWebSocket(),
) : OkHttpClient() {

    private lateinit var request: Request
    private lateinit var listener: WebSocketListener

    override fun newWebSocket(request: Request, listener: WebSocketListener): WebSocket {
        this.request = request
        this.listener = listener
        return webSocket
    }

    fun failWebSocketConnection() {
        listener.onFailure(webSocket = webSocket, t = RuntimeException(""), response = null)
    }
}
