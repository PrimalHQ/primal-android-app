package net.primal.core.networking.blossom

import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asExpirationTag
import net.primal.domain.nostr.asHashtagTag
import net.primal.domain.nostr.asSha256Tag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import okio.Buffer
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import okio.HashingSource
import okio.blackholeSink
import okio.buffer

private const val DEFAULT_BUFFER_SIZE = 8 * 1024

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
        val source = inputStream()

        val authorizationHeader = createAuthorizationHeader(
            pubkey = userId,
            hash = uploadFileMetadata.sha256,
        )

        val totalBytes = uploadFileMetadata.sizeInBytes
        var uploadedBytes = 0L

        val httpClient = HttpClientFactory.createHttpClientWithDefaultConfig()

        val response = httpClient.put("$blossomUrl/upload") {
            headers {
                append("Authorization", authorizationHeader)
                append("Content-Length", totalBytes.toString())
            }

            setBody(object : OutgoingContent.WriteChannelContent() {
                override val contentType: ContentType
                    get() = ContentType.Application.OctetStream

                override suspend fun writeTo(channel: ByteWriteChannel) {
                    val byteArray = ByteArray(DEFAULT_BUFFER_SIZE)

                    while (!source.exhausted()) {
                        val read = source.read(byteArray)
                        if (read == -1) break

                        channel.writeFully(byteArray, 0, read)
                        uploadedBytes += read
                        onProgress?.invoke(uploadedBytes.toInt(), totalBytes.toInt())
                    }
                    channel.flushAndClose()
                }
            })
        }

        if (!response.status.isSuccess()) {
            val reason = response.headers["X-Reason"] ?: "Unknown"
            throw UnsuccessfulBlossomUpload(Exception("Upload failed: ${response.status.value} - $reason"))
        }

        val descriptor = response.body<BlobDescriptor>()

        if (uploadFileMetadata.sizeInBytes != descriptor.sizeInBytes) {
            throw UnsuccessfulBlossomUpload(
                cause = RuntimeException("Different file size on the server."),
            )
        }

        return descriptor
    }

    private fun BufferedSource.getMetadata(): UploadFileMetadata {
        val hashingSource = HashingSource.sha256(this)
        val bufferedHashingSource = hashingSource.buffer()
        val blackhole = blackholeSink().buffer()
        val tempBuffer = Buffer()

        var totalBytes = 0L
        while (!bufferedHashingSource.exhausted()) {
            val bytesRead = bufferedHashingSource.read(tempBuffer, 8192)
            if (bytesRead == -1L) break
            totalBytes += bytesRead
            blackhole.write(tempBuffer, bytesRead)
        }

        val sha256Bytes = hashingSource.hash.toByteArray()
        val hex = sha256Bytes.joinToString("") {
            it.toInt().and(0xff).toString(16).padStart(2, '0')
        }

        return UploadFileMetadata(
            sizeInBytes = totalBytes,
            sha256 = hex,
        )
    }

    private data class UploadFileMetadata(
        val sizeInBytes: Long,
        val sha256: String,
    )

    private fun createAuthorizationHeader(pubkey: String, hash: String): String {
        val signed = signatureHandler.signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                kind = NostrEventKind.BlossomUploadBlob.value,
                pubKey = pubkey,
                content = "Upload File",
                tags = listOf(
                    "upload".asHashtagTag(),
                    hash.asSha256Tag(),
                    expirationTimestamp().asExpirationTag(),
                ),
            ),
        )

        val jsonPayload = signed.encodeToJsonString()
        val base64Encoded = jsonPayload.encodeUtf8().base64()

        return "Nostr $base64Encoded"
    }

    private fun expirationTimestamp() = Clock.System.now().plus(1.hours).toEpochMilliseconds()
}
