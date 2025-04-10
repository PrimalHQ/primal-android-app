package net.primal.android.networking.primal.upload

import android.content.ContentResolver
import android.net.Uri
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import net.primal.android.nostr.notary.NostrNotary
import net.primal.core.networking.primal.api.BlossomUploadApi
import net.primal.core.networking.primal.api.model.BlobDescriptor
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.upload.UploadResult

class PrimalBlossomFileUploader @Inject constructor(
    private val contentResolver: ContentResolver,
    private val dispatcherProvider: DispatcherProvider,
    private val uploadApi: BlossomUploadApi,
    private val nostrNotary: NostrNotary,
) {
    private companion object {
        private const val MILLISECONDS_IN_SECOND = 1000
        private const val ONE_HOUR_IN_SECONDS = 3600
    }

    suspend fun uploadFile(
        uri: Uri,
        keyPair: NostrKeyPair,
        mimeType: String?,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult =
        withContext(dispatcherProvider.io()) {
            val inputStream = contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Can't open file")
            val fileSize = uri.readFileSizeInBytes()
            val fileBytes = inputStream.readBytes()
            val hash = calculateSha256(fileBytes)

            val authorizationHeader = createAuthorizationHeader(
                pubkey = keyPair.pubKey,
                filename = getFileName(uri),
                hash = hash,
            )

            onProgress?.invoke(0, fileSize.toInt())

            val descriptor: BlobDescriptor = uploadApi.uploadBlob(
                data = fileBytes,
                mimeType = mimeType,
                authorization = authorizationHeader,
            )

            onProgress?.invoke(fileSize.toInt(), fileSize.toInt())

            UploadResult(
                remoteUrl = descriptor.url,
                originalFileSize = descriptor.size,
                originalHash = descriptor.sha256,
            )
        }

    private suspend fun createAuthorizationHeader(
        pubkey: String,
        filename: String,
        hash: String,
    ): String {
        val unsignedEvent = NostrUnsignedEvent(
            kind = 24242,
            pubKey = pubkey,
            content = "Upload $filename",
            tags = listOf(
                listOf("t", "upload"),
                listOf("x", hash),
                listOf(
                    "expiration",
                    (System.currentTimeMillis() / MILLISECONDS_IN_SECOND + ONE_HOUR_IN_SECONDS).toString(),
                ),
            ).map { tagList -> JsonArray(tagList.map { JsonPrimitive(it) }) },
        )
        val signed = nostrNotary.signNostrEvent(pubkey, unsignedEvent)
        return "Nostr ${Base64.getEncoder().encodeToString(signed.encodeToJsonString().toByteArray())}"
    }

    private fun calculateSha256(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }

    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment?.substringAfterLast("/") ?: "file"
    }

    private fun Uri.readFileSizeInBytes(): Long {
        contentResolver.openFileDescriptor(this, "r")?.use {
            return it.statSize
        }
        throw IllegalArgumentException("Could not determine file size")
    }
}
