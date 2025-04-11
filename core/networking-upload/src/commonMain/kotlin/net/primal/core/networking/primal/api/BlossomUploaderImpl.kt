package net.primal.core.networking.primal.api

import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.networking.primal.api.model.BlobDescriptor
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import okio.BufferedSource

internal class BlossomUploaderImpl(
    private val signatureHandler: NostrEventSignatureHandler,
) : BlossomUploader {

    override suspend fun uploadBlob(
        userId: String,
        blossomUrl: String,
        inputStream: () -> BufferedSource,
        onProgress: ((Int, Int) -> Unit)?,
    ): BlobDescriptor {
        val uploadFileMetadata = inputStream().getMetadata()

        val authorizationHeader = createAuthorizationHeader(
            pubkey = userId,
            hash = uploadFileMetadata.sha256,
        )

        // Upload logic
        // TODO Implement uploading to blossom server
        val httpClient = HttpClientFactory.createHttpClientWithDefaultConfig()
        val descriptor: BlobDescriptor = throw NotImplementedError()

        if (uploadFileMetadata.sizeInBytes != descriptor.sizeInBytes) {
            throw UnsuccessfulFileUpload(
                cause = RuntimeException("Different file size on the server."),
            )
        }

        throw NotImplementedError()

//        val client = clientsChannel.receive()
//        return try {
//            client.put(uploadUrl) {
//                setBody(data)
//                mimeType?.let { contentType(ContentType.parse(it)) }
//                authorization?.let { headers.append(HttpHeaders.Authorization, it) }
//                headers.append(HttpHeaders.ContentLength, data.size.toString())
//            }.let { response ->
//                if (!response.status.isSuccess()) {
//                    val reason = response.headers["X-Reason"] ?: "Upload failed"
//                    throw UnsuccessfulFileUpload(Exception("Error ${response.status.value}: $reason"))
//                }
//
//                response.body()
//            }
//        } catch (e: Exception) {
//            throw UnsuccessfulFileUpload(e)
//        } finally {
//            clientsChannel.send(client)
//        }
    }

    private fun BufferedSource.getMetadata(): UploadFileMetadata {
        // TODO Implement resolving metadata (size in bytes and sha256)
        return UploadFileMetadata(
            sizeInBytes = 100,
            sha256 = "123",
        )
    }

    private data class UploadFileMetadata(
        val sizeInBytes: Long,
        val sha256: String,
    )

    private fun createAuthorizationHeader(pubkey: String, hash: String): String {
        val signed = signatureHandler.signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                kind = 24242,
                pubKey = pubkey,
                content = "Upload File",
                tags = listOf(
                    listOf("t", "upload"),
                    listOf("x", hash),
                    listOf("expiration", expirationTimestamp()),
                ).map { tagList -> JsonArray(tagList.map { JsonPrimitive(it) }) },
            ),
        )
        return ""
        // TODO Find a way for base64 for pure kotlin
//        return "Nostr ${Base64.getEncoder().encodeToString(signed.encodeToJsonString().toByteArray())}"
    }

    private fun expirationTimestamp(): String = Clock.System.now().plus(1.hours).toEpochMilliseconds().toString()
}
