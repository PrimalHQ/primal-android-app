package net.primal.android.networking.upload

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.android.nostr.notary.signOrThrow
import net.primal.core.networking.blossom.BlossomApiFactory
import net.primal.core.networking.blossom.FileMetadata
import net.primal.core.networking.blossom.UnsuccessfulBlossomUpload
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
    private val signatureHandler: NostrEventSignatureHandler,
) {

    private val blossomApi = BlossomApiFactory.create(baseBlossomUrl = "https://blossom.primal.net")

    @SuppressLint("Recycle")
    private fun Uri.openBufferedSource(): BufferedSource {
        return contentResolver.openInputStream(this)?.source()?.buffer()
            ?: throw IOException("Unable to open input stream.")
    }

    @Throws(UnsuccessfulBlossomUpload::class)
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

    private suspend fun upload(
        uri: Uri,
        userId: String,
        onSignRequested: (NostrUnsignedEvent) -> NostrEvent,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult =
        withContext(dispatchers.io()) {
            val fileMetadata = uri.openBufferedSource().use { it.getMetadata() }

            val signed = onSignRequested(
                NostrUnsignedEvent(
                    kind = NostrEventKind.BlossomUploadBlob.value,
                    pubKey = userId,
                    content = "Upload File",
                    tags = listOf(
                        "upload".asHashtagTag(),
                        fileMetadata.sha256.asSha256Tag(),
                        expirationTimestamp().asExpirationTag(),
                    ),
                ),
            )

            val jsonPayload = signed.encodeToJsonString()
            val base64Encoded = jsonPayload.encodeUtf8().base64()
            val authorizationHeader = "Nostr $base64Encoded"

            val descriptor = blossomApi.putUpload(
                authorization = authorizationHeader,
                fileMetadata = fileMetadata,
                openBufferedSource = { uri.openBufferedSource() },
                onProgress = onProgress,
            )

            UploadResult(
                remoteUrl = descriptor.url,
                originalFileSize = descriptor.sizeInBytes,
                originalHash = descriptor.sha256,
            )
        }

    private fun expirationTimestamp() = Clock.System.now().plus(1.hours).toEpochMilliseconds()

    private fun BufferedSource.getMetadata(): FileMetadata {
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

        return FileMetadata(
            sizeInBytes = totalBytes,
            sha256 = hex,
        )
    }
}
