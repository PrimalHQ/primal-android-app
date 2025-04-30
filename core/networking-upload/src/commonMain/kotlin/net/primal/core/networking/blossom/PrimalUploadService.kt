package net.primal.core.networking.blossom

import io.github.aakira.napier.Napier
import io.github.anvell.filetype.FileType
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asExpirationTag
import net.primal.domain.nostr.asHashtagTag
import net.primal.domain.nostr.asSha256Tag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import okio.Buffer
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import okio.HashingSource
import okio.blackholeSink
import okio.buffer
import okio.use

internal class PrimalUploadService(
    private val dispatchers: DispatcherProvider,
    private val blossomResolver: BlossomServerListProvider,
    private val signatureHandler: NostrEventSignatureHandler? = null,
) {

    private val mirroringScope = CoroutineScope(SupervisorJob() + dispatchers.io())

    suspend fun upload(
        userId: String,
        openBufferedSource: () -> BufferedSource,
        onSignRequested: (suspend (NostrUnsignedEvent) -> NostrEvent)? = null,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult {
        val result = withContext(dispatchers.io()) {
            runCatching {
                val blossomApis = resolveBlossomApisOrThrow(userId = userId)
                val primaryApi = blossomApis.first()
                val mirrorApis = blossomApis.drop(1)

                val fileMetadata = openBufferedSource().use { it.getMetadata() }

                val uploadAuthorizationHeader = signAuthorizationOrThrow(
                    userId = userId,
                    fileHash = fileMetadata.sha256,
                    onSignRequested = onSignRequested,
                )

                val descriptor: BlobDescriptor = try {
//                    primaryApi.headMedia(
//                        authorization = uploadAuthorizationHeader,
//                        fileMetadata = fileMetadata,
//                    )
                    primaryApi.putMedia(
                        authorization = uploadAuthorizationHeader,
                        fileMetadata = fileMetadata,
                        bufferedSource = openBufferedSource(),
                        onProgress = onProgress,
                    )
                } catch (_: BlossomException) {
//                    primaryApi.headUpload(
//                        authorization = uploadAuthorizationHeader,
//                        fileMetadata = fileMetadata,
//                    )
                    primaryApi.putUpload(
                        authorization = uploadAuthorizationHeader,
                        fileMetadata = fileMetadata,
                        bufferedSource = openBufferedSource(),
                        onProgress = onProgress,
                    )
                }

                val mirrorAuthorizationHeader = signAuthorizationOrThrow(
                    userId = userId,
                    fileHash = descriptor.sha256,
                    humanMessage = "Mirror File",
                    onSignRequested = onSignRequested,
                )

                mirrorApis.forEach { blossomApi ->
                    mirroringScope.launch {
                        runCatching {
                            blossomApi.putMirror(
                                authorization = mirrorAuthorizationHeader,
                                fileUrl = descriptor.url,
                            )
                        }.onFailure { error ->
                            Napier.w(error) { "Blossom mirror failed for ${descriptor.url}" }
                        }
                    }
                }

                return@runCatching descriptor
            }
        }

        return try {
            val descriptor = result.getOrThrow()
            UploadResult.Success(
                remoteUrl = descriptor.url,
                originalFileSize = descriptor.sizeInBytes,
                originalHash = descriptor.sha256,
            )
        } catch (error: Exception) {
            val uploadError = when {
                error is BlossomException -> error
                else -> BlossomException(cause = error)
            }
            UploadResult.Failed(
                error = uploadError,
                message = uploadError.message,
            )
        }
    }

    private suspend fun resolveBlossomApisOrThrow(userId: String): List<BlossomApi> {
        return blossomResolver.provideBlossomServerList(userId).mapNotNull {
            runCatching { BlossomApiFactory.create(baseBlossomUrl = it.trimTrailingSlashes()) }.getOrNull()
        }.ifEmpty {
            throw BlossomUploadException(cause = IllegalStateException("Invalid blossom server list."))
        }
    }

    private suspend fun signAuthorizationOrThrow(
        userId: String,
        fileHash: String,
        humanMessage: String? = null,
        onSignRequested: (suspend (NostrUnsignedEvent) -> NostrEvent)? = null,
    ): String {
        val unsignedAuthorizationEvent = NostrUnsignedEvent(
            kind = NostrEventKind.BlossomUploadBlob.value,
            pubKey = userId,
            content = humanMessage ?: "Upload File",
            tags = listOf(
                "upload".asHashtagTag(),
                fileHash.asSha256Tag(),
                expirationTimestamp().asExpirationTag(),
            ),
        )

        val signedAuthorizationEvent = onSignRequested?.invoke(unsignedAuthorizationEvent)
            ?: signatureHandler?.signNostrEvent(unsignedAuthorizationEvent)?.unwrapOrThrow()
            ?: error("Missing signature handler.")
        return signedAuthorizationEvent.buildAuthorizationHeader()
    }

    private fun NostrEvent.buildAuthorizationHeader(): String {
        val jsonPayload = this.encodeToJsonString()
        val base64Encoded = jsonPayload.encodeUtf8().base64()
        val authorizationHeader = "Nostr $base64Encoded"
        return authorizationHeader
    }

    private fun expirationTimestamp() = Clock.System.now().plus(1.hours).toEpochMilliseconds()

    private fun BufferedSource.getMetadata(): FileMetadata {
        val hashingSource = HashingSource.sha256(this)
        val bufferedHashingSource = hashingSource.buffer()
        val blackhole = blackholeSink().buffer()
        val tempBuffer = Buffer()

        val mimeType = this.mimeType()
        var totalBytes = 0L
        bufferedHashingSource.use {
            while (!it.exhausted()) {
                val bytesRead = it.read(tempBuffer, DEFAULT_BUFFER_SIZE)
                if (bytesRead == -1L) break
                totalBytes += bytesRead
                blackhole.write(tempBuffer, bytesRead)
            }
        }

        val sha256Bytes = hashingSource.hash.toByteArray()
        val hex = sha256Bytes.joinToString("") {
            it.toInt().and(other = 0xff).toString(radix = 16).padStart(length = 2, padChar = '0')
        }

        return FileMetadata(
            sizeInBytes = totalBytes,
            sha256 = hex,
            mimeType = mimeType,
        )
    }

    /**
     * Returns the RFC-6838 media-type (e.g. "image/png") or `null`
     * without consuming the source.
     */
    fun BufferedSource.mimeType(): String? {
        val head = peek().readByteArray(512)
        return FileType.detect(head)?.toString()
    }

    private companion object {
        private const val DEFAULT_BUFFER_SIZE = 8 * 1024L
    }

    private fun String.trimTrailingSlashes(): String = this.trimEnd('/')
}
