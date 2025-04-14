package net.primal.android.networking.upload

import android.content.ContentResolver
import android.net.Uri
import io.github.aakira.napier.Napier
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.android.nostr.notary.signOrThrow
import net.primal.android.user.repository.BlossomRepository
import net.primal.core.networking.blossom.BlobDescriptor
import net.primal.core.networking.blossom.BlossomApi
import net.primal.core.networking.blossom.BlossomApiFactory
import net.primal.core.networking.blossom.BlossomException
import net.primal.core.networking.blossom.BlossomUploadException
import net.primal.core.networking.blossom.FileMetadata
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asExpirationTag
import net.primal.domain.nostr.asHashtagTag
import net.primal.domain.nostr.asSha256Tag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.utils.hexToNsecHrp
import okio.Buffer
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import okio.HashingSource
import okio.blackholeSink
import okio.buffer
import okio.source

class PrimalUploadService @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val contentResolver: ContentResolver,
    private val blossomRepository: BlossomRepository,
    private val signatureHandler: NostrEventSignatureHandler,
) {

    private val mirroringScope = CoroutineScope(SupervisorJob() + dispatchers.io())

    private fun Uri.openBufferedSource(): BufferedSource {
        return contentResolver.openInputStream(this)?.source()?.buffer()
            ?: throw IOException("Unable to open input stream.")
    }

    @Throws(BlossomException::class, CancellationException::class)
    suspend fun upload(
        uri: Uri,
        userId: String,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult =
        upload(
            uri = uri,
            userId = userId,
            onSignRequested = { signatureHandler.signNostrEvent(it) },
            onProgress = onProgress,
        )

    suspend fun upload(
        uri: Uri,
        keyPair: NostrKeyPair,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult =
        upload(
            uri = uri,
            userId = keyPair.pubKey,
            onSignRequested = { unsignedNostrEvent ->
                unsignedNostrEvent.signOrThrow(keyPair.privateKey.hexToNsecHrp())
            },
            onProgress = onProgress,
        )

    @Throws(BlossomException::class, CancellationException::class)
    private suspend fun upload(
        uri: Uri,
        userId: String,
        onSignRequested: (NostrUnsignedEvent) -> NostrEvent,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult =
        withContext(dispatchers.io()) {
            val blossomApis = resolveBlossomApisOrThrow(userId = userId)
            val primaryApi = blossomApis.first()
            val mirrorApis = blossomApis.drop(1)

            val fileMetadata = uri.openBufferedSource().use { it.getMetadata() }
            val uploadAuthorizationHeader = onSignRequested(
                buildAuthorizationUnsignedNostrEvent(
                    userId = userId,
                    fileHash = fileMetadata.sha256,
                ),
            ).buildAuthorizationHeader()

            val descriptor: BlobDescriptor = try {
                primaryApi.headMedia(
                    authorization = uploadAuthorizationHeader,
                    fileMetadata = fileMetadata,
                )
                primaryApi.putMedia(
                    authorization = uploadAuthorizationHeader,
                    fileMetadata = fileMetadata,
                    openBufferedSource = { uri.openBufferedSource() },
                    onProgress = onProgress,
                )
            } catch (_: BlossomException) {
                primaryApi.headUpload(
                    authorization = uploadAuthorizationHeader,
                    fileMetadata = fileMetadata,
                )
                primaryApi.putUpload(
                    authorization = uploadAuthorizationHeader,
                    fileMetadata = fileMetadata,
                    openBufferedSource = { uri.openBufferedSource() },
                    onProgress = onProgress,
                )
            }

            val mirrorAuthorizationHeader = onSignRequested(
                buildAuthorizationUnsignedNostrEvent(
                    userId = userId,
                    fileHash = descriptor.sha256,
                    humanMessage = "Mirror File",
                ),
            ).buildAuthorizationHeader()

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

            UploadResult(
                remoteUrl = descriptor.url,
                originalFileSize = descriptor.sizeInBytes,
                originalHash = descriptor.sha256,
            )
        }

    private suspend fun resolveBlossomApisOrThrow(userId: String): List<BlossomApi> {
        return blossomRepository.ensureBlossomServerList(userId).mapNotNull {
            runCatching { BlossomApiFactory.create(baseBlossomUrl = it) }.getOrNull()
        }.ifEmpty {
            throw BlossomUploadException(cause = IllegalStateException("Invalid blossom server list."))
        }
    }

    private fun buildAuthorizationUnsignedNostrEvent(
        userId: String,
        fileHash: String,
        humanMessage: String? = null,
    ): NostrUnsignedEvent =
        NostrUnsignedEvent(
            kind = NostrEventKind.BlossomUploadBlob.value,
            pubKey = userId,
            content = humanMessage ?: "Upload File",
            tags = listOf(
                "upload".asHashtagTag(),
                fileHash.asSha256Tag(),
                expirationTimestamp().asExpirationTag(),
            ),
        )

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
        )
    }

    private companion object {
        private const val DEFAULT_BUFFER_SIZE = 8 * 1024L
    }
}
