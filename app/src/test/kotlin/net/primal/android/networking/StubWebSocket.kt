package net.primal.android.networking

import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

class StubWebSocket(private val request: Request) : WebSocket {

    override fun cancel() = Unit

    override fun close(code: Int, reason: String?): Boolean = true

    override fun queueSize(): Long = 0

    override fun request(): Request = request

    override fun send(text: String): Boolean = true

    override fun send(bytes: ByteString): Boolean = true
}
