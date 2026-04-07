package net.primal.data.repository.nip05

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import net.primal.core.networking.factory.HttpClientFactory

private const val TAG = "Nip05HttpClient"
private const val MAX_RESPONSE_SIZE_BYTES = 2 * 1024 * 1024L // 2 MB

class Nip05HttpClient(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun fetchWellKnown(domain: String, name: String): Nip05WellKnownResponse? {
        val url = "https://$domain/.well-known/nostr.json"
        Napier.d(tag = TAG) { "Fetching $url?name=$name" }

        val statement = httpClient.prepareGet(url) {
            parameter("name", name)
        }

        return statement.execute { response ->
            if (!response.status.isSuccess()) {
                Napier.d(tag = TAG) { "HTTP ${response.status} for $url" }
                return@execute null
            }

            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
            if (contentLength != null && contentLength > MAX_RESPONSE_SIZE_BYTES) {
                Napier.d(tag = TAG) { "Response too large ($contentLength bytes) for $url" }
                return@execute null
            }

            val channel = response.bodyAsChannel()
            val packet = channel.readRemaining(MAX_RESPONSE_SIZE_BYTES)
            val bytes = packet.readByteArray()

            if (!channel.isClosedForRead) {
                Napier.d(tag = TAG) { "Response exceeded $MAX_RESPONSE_SIZE_BYTES bytes for $url" }
                return@execute null
            }

            json.decodeFromString<Nip05WellKnownResponse>(bytes.decodeToString())
        }
    }

    companion object {
        fun create(): Nip05HttpClient {
            val httpClient = HttpClientFactory.createHttpClient {
                followRedirects = false
                install(HttpTimeout) {
                    requestTimeoutMillis = 10_000
                    connectTimeoutMillis = 10_000
                }
            }
            return Nip05HttpClient(httpClient)
        }
    }
}
