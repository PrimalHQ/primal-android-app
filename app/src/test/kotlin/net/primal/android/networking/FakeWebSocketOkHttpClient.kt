package net.primal.android.networking

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class FakeWebSocketOkHttpClient : OkHttpClient() {

    private lateinit var request: Request
    private lateinit var listener: WebSocketListener
    private lateinit var webSocket: WebSocket

    override fun newWebSocket(request: Request, listener: WebSocketListener): WebSocket {
        this.request = request
        this.listener = listener
        this. webSocket = StubWebSocket(request = request)
        return webSocket
    }

    fun failWebSocketConnection() {
        listener.onFailure(webSocket = webSocket, t = RuntimeException(""), response = null)
    }
}
