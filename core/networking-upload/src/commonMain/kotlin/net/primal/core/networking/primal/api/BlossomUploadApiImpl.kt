package net.primal.core.networking.primal.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import net.primal.core.networking.primal.api.model.BlobDescriptor
import net.primal.core.networking.primal.api.model.UnsuccessfulFileUpload

class BlossomUploadApiImpl(
    private val uploadUrl: String = "blossoms.primal.net",
    private val poolSize: Int = 3,
) : BlossomUploadApi {

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val clientsChannel = Channel<HttpClient>(Channel.UNLIMITED)

    init {
        repeat(poolSize) {
            clientsChannel.trySend(
                HttpClient {
                    install(ContentNegotiation) {
                        json(jsonParser)
                    }
                },
            )
        }
    }

    override suspend fun uploadBlob(
        data: ByteArray,
        mimeType: String?,
        authorization: String?,
    ): BlobDescriptor {
        val client = clientsChannel.receive()
        return try {
            client.put(uploadUrl) {
                setBody(data)
                mimeType?.let { contentType(ContentType.parse(it)) }
                authorization?.let { headers.append(HttpHeaders.Authorization, it) }
                headers.append(HttpHeaders.ContentLength, data.size.toString())
            }.let { response ->
                if (!response.status.isSuccess()) {
                    val reason = response.headers["X-Reason"] ?: "Upload failed"
                    throw UnsuccessfulFileUpload(Exception("Error ${response.status.value}: $reason"))
                }

                response.body()
            }
        } catch (e: Exception) {
            throw UnsuccessfulFileUpload(e)
        } finally {
            clientsChannel.send(client)
        }
    }
}
