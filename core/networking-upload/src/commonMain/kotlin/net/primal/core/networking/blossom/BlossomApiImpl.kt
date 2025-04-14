package net.primal.core.networking.blossom

import io.ktor.client.call.body
import io.ktor.client.request.head
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

    override suspend fun headMedia(authorization: String, fileMetadata: FileMetadata) =
        performHeadRequest(
            endpoint = "media",
            authorization = authorization,
            fileMetadata = fileMetadata,
            errorPrefix = "Head Media",
        )

    override suspend fun headUpload(authorization: String, fileMetadata: FileMetadata) =
        performHeadRequest(
            endpoint = "upload",
            authorization = authorization,
            fileMetadata = fileMetadata,
            errorPrefix = "Head Upload",
        )

    override suspend fun putUpload(
        authorization: String,
        fileMetadata: FileMetadata,
        openBufferedSource: () -> BufferedSource,
        onProgress: ((Int, Int) -> Unit)?,
    ): BlobDescriptor =
        performPutUpload(
            "upload",
            authorization = authorization,
            contentType = "application/octet-stream",
            fileMetadata = fileMetadata,
            openBufferedSource = openBufferedSource,
            onProgress = onProgress,
            errorPrefix = "Upload",
            checkFileSize = true,
        )

    override suspend fun putMedia(
        authorization: String,
        fileMetadata: FileMetadata,
        openBufferedSource: () -> BufferedSource,
        onProgress: ((Int, Int) -> Unit)?,
    ): BlobDescriptor =
        performPutUpload(
            endpoint = "media",
            authorization = authorization,
            contentType = fileMetadata.mimeType ?: "application/octet-stream",
            fileMetadata = fileMetadata,
            openBufferedSource = openBufferedSource,
            onProgress = onProgress,
            errorPrefix = "Upload Media",
        )

    override suspend fun putMirror(authorization: String, fileUrl: String): BlobDescriptor {
        val response = withContext(dispatcherProvider.io()) {
            httpClient.put("$baseBlossomUrl/mirror") {
                headers {
                    append("Authorization", authorization)
                }
                setBody(MirrorRequest(fileUrl))
            }
        }

        if (!response.status.isSuccess()) {
            val reason = response.headers["X-Reason"] ?: "Unknown"
            throw BlossomMirrorException(message = reason)
        }

        return response.body<BlobDescriptor>()
    }

    private suspend fun performHeadRequest(
        endpoint: String,
        authorization: String,
        fileMetadata: FileMetadata,
        errorPrefix: String,
    ) {
        val response = withContext(dispatcherProvider.io()) {
            httpClient.head("$baseBlossomUrl/$endpoint") {
                headers {
                    append("Authorization", authorization)
                    append("X-SHA-256", fileMetadata.sha256)
                    append("X-Content-Length", fileMetadata.sizeInBytes.toString())
                    append("X-Content-Type", fileMetadata.mimeType ?: "application/octet-stream")
                }
            }
        }

        if (!response.status.isSuccess()) {
            val reason = response.headers["X-Reason"] ?: "Unknown"
            throw UploadRequirementException(message = "$reason ($errorPrefix)")
        }
    }

    private suspend fun performPutUpload(
        endpoint: String,
        authorization: String,
        contentType: String,
        fileMetadata: FileMetadata,
        openBufferedSource: () -> BufferedSource,
        errorPrefix: String,
        onProgress: ((Int, Int) -> Unit)? = null,
        checkFileSize: Boolean = false,
    ): BlobDescriptor {
        var uploadedBytes = 0L
        val totalBytes = fileMetadata.sizeInBytes

        val response = withContext(dispatcherProvider.io()) {
            openBufferedSource().use { source ->
                httpClient.put("$baseBlossomUrl/$endpoint") {
                    headers {
                        append("Authorization", authorization)
                        append("Content-Length", totalBytes.toString())
                        append("Content-Type", contentType)
                    }

                    setBody(object : OutgoingContent.WriteChannelContent() {
                        override val contentType: ContentType
                            get() = ContentType.parse(contentType)

                        override suspend fun writeTo(channel: ByteWriteChannel) {
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (!source.exhausted()) {
                                val read = source.read(buffer)
                                if (read == -1) break
                                channel.writeFully(buffer, 0, read)
                                uploadedBytes += read
                                onProgress?.invoke(uploadedBytes.toInt(), totalBytes.toInt())
                            }
                            channel.flush()
                        }
                    })
                }
            }
        }

        if (!response.status.isSuccess()) {
            val reason = response.headers["X-Reason"] ?: "Unknown"
            throw BlossomUploadException(message = "$reason ($errorPrefix)")
        }

        val descriptor = response.body<BlobDescriptor>()
        if (checkFileSize && fileMetadata.sizeInBytes != descriptor.sizeInBytes) {
            throw BlossomUploadException(message = "Different file size on the server.")
        }

        return descriptor
    }
}
