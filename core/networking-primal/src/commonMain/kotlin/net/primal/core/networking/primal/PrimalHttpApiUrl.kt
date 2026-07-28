package net.primal.core.networking.primal

import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.set

/**
 * Required by Ktorfit, but never used to build a request: every call passes an absolute url
 * resolved through [asPrimalHttpApiUrl].
 */
internal const val DEFAULT_HTTP_API_BASE_URL = "https://cache1.primal.net/"

private const val HTTP_API_PATH = "/api"

/**
 * Maps a Primal server socket url to its HTTP counterpart, e.g. `wss://cache1.primal.net/v1` ->
 * `https://cache1.primal.net/api`. Derived rather than configured because
 * `.well-known/primal-endpoints.json` only publishes socket urls.
 */
internal fun String.asPrimalHttpApiUrl(): String {
    val source = URLBuilder(this)
    val secure = source.protocol == URLProtocol.WSS || source.protocol == URLProtocol.HTTPS
    return URLBuilder().apply {
        set(
            scheme = if (secure) URLProtocol.HTTPS.name else URLProtocol.HTTP.name,
            host = source.host,
            port = source.port.takeIf { it != source.protocol.defaultPort },
            path = HTTP_API_PATH,
        )
    }.buildString()
}
