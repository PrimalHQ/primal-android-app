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
import kotlinx.coroutines.withContext
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.core.utils.coroutines.DispatcherProvider
import okio.BufferedSource
import okio.use

private const val DEFAULT_BUFFER_SIZE = 8 * 1024

internal class BlossomApiImpl(
    private val baseBlossomUrl: String,
    private val dispatcherProvider: DispatcherProvider,
) : BlossomApi {

    private val httpClient by lazy {
        HttpClientFactory.createHttpClientWithDefaultConfig()
    }

    override suspend fun headUpload(authorization: String, fileMetadata: FileMetadata) {
        throw NotImplementedError()
    }

    override suspend fun putUpload(
        authorization: String,
        fileMetadata: FileMetadata,
        openBufferedSource: () -> BufferedSource,
        onProgress: ((Int, Int) -> Unit)?,
    ): BlobDescriptor {
        var uploadedBytes = 0L
        val totalBytes = fileMetadata.sizeInBytes
        val response = withContext(dispatcherProvider.io()) {
            openBufferedSource().use { source ->
                httpClient.put("$baseBlossomUrl/upload") {
                    headers {
                        append("Authorization", authorization)
                        append("Content-Length", totalBytes.toString())
                    }

                    setBody(
                        object : OutgoingContent.WriteChannelContent() {
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
                        },
                    )
                }
            }
        }

        if (!response.status.isSuccess()) {
            val reason = response.headers["X-Reason"] ?: "Unknown"
            throw UnsuccessfulBlossomUpload(Exception("Upload failed: ${response.status.value} - $reason"))
        }

        val descriptor = response.body<BlobDescriptor>()
        if (fileMetadata.sizeInBytes != descriptor.sizeInBytes) {
            throw UnsuccessfulBlossomUpload(
                cause = RuntimeException("Different file size on the server."),
            )
        }

        return descriptor
    }

    override suspend fun headMedia(authorization: String, fileMetadata: FileMetadata) {
        throw NotImplementedError()
    }

    override suspend fun putMedia(
        authorization: String,
        fileMetadata: FileMetadata,
        inputStream: () -> BufferedSource,
        onProgress: ((Int, Int) -> Unit)?,
    ): BlobDescriptor {
        throw NotImplementedError()
    }

    override suspend fun putMirror(authorization: String, fileUrl: String): BlobDescriptor {
        throw NotImplementedError()
    }
}
