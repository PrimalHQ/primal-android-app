package net.primal.android.networking

import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

class FakeWebSocket(
    private val sendResponse: Boolean = true,
) : WebSocket {

    override fun cancel() = Unit

    override fun close(code: Int, reason: String?): Boolean = true

    override fun queueSize(): Long = 0

    override fun request(): Request = Request.Builder().url("wss://relay.primal.net").build()

    override fun send(text: String): Boolean = sendResponse

    override fun send(bytes: ByteString): Boolean = sendResponse
}
