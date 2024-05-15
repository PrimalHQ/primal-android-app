package net.primal.android.core.files

import android.content.ContentResolver
import android.net.Uri
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.files.api.UploadApi
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.core.files.model.chunkUploadRequest
import net.primal.android.core.files.model.completeUploadRequest
import net.primal.android.crypto.NostrKeyPair
import net.primal.android.crypto.hexToNsecHrp
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.nostr.notary.signOrThrow

@Singleton
class ChunkedFileUploader @Inject constructor(
    private val contentResolver: ContentResolver,
    private val dispatchers: CoroutineDispatcherProvider,
    private val uploadApi: UploadApi,
    private val nostrNotary: NostrNotary,
) {

    companion object {
        private const val PARALLEL_UPLOADS = 5
        private const val KB = 1024
        private const val MB = 1024 * KB
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadFile(keyPair: NostrKeyPair, uri: Uri): String {
        val userId = keyPair.pubKey
        return uploadFileOrThrow(
            uri = uri,
            userId = userId,
            signNostrEvent = { it.signOrThrow(keyPair.privateKey.hexToNsecHrp()) },
        )
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadFile(userId: String, uri: Uri): String {
        return uploadFileOrThrow(
            uri = uri,
            userId = userId,
            signNostrEvent = { nostrNotary.signNostrEvent(userId = userId, event = it) },
        )
    }

    private suspend fun uploadFileOrThrow(
        uri: Uri,
        userId: String,
        signNostrEvent: (NostrUnsignedEvent) -> NostrEvent,
    ): String {
        val uploadId = UUID.randomUUID().toString()
        val fileDigest = MessageDigest.getInstance("SHA-256")
        return withContext(dispatchers.io()) {
            try {
                val fileSizeInBytes = uri.readFileSizeInBytes()
                val chunkSize = calculateChunkSize(fileSizeInBytes)
                contentResolver.openInputStream(uri).use { inputStream ->
                    if (inputStream == null) throw FileNotFoundException()
                    inputStream.toChunkedFlow(fileSizeInBytes = fileSizeInBytes, chunkSize = chunkSize)
                        .withIndex()
                        .map {
                            val offset = it.index * chunkSize
                            fileDigest.update(it.value, 0, it.value.size)
                            async(dispatchers.io()) {
                                uploadApi.uploadChunk(
                                    signNostrEvent(
                                        chunkUploadRequest(
                                            userId = userId,
                                            uploadId = uploadId,
                                            fileSizeInBytes = fileSizeInBytes,
                                            offsetInBytes = offset,
                                            data = it.value,
                                        ),
                                    ),
                                )
                            }
                        }
                        .buffer(capacity = PARALLEL_UPLOADS)
                        .collect { deferred -> deferred.await() }
                }

                val hash = fileDigest.digestAsString()
                uploadApi.completeUpload(
                    signNostrEvent(
                        completeUploadRequest(
                            userId = userId,
                            uploadId = uploadId,
                            fileSizeInBytes = fileSizeInBytes,
                            hash = hash,
                        ),
                    ),
                )
            } catch (error: IOException) {
                throw UnsuccessfulFileUpload(cause = error)
            }
        }
    }

    @Throws(IOException::class)
    private fun Uri.readFileSizeInBytes(): Long {
        contentResolver.openFileDescriptor(this, "r")?.use {
            FileInputStream(it.fileDescriptor).use { inputStream ->
                return inputStream.channel.size()
            }
        }
        throw FileNotFoundException()
    }

    @Suppress("MagicNumber")
    private fun calculateChunkSize(fileSizeInBytes: Long): Int {
        return when (fileSizeInBytes) {
            in 0 until MB / 2 -> fileSizeInBytes.toInt()
            in MB / 2 until 2 * MB -> MB / 4
            in 2 * MB until 8 * MB -> MB / 2
            else -> MB
        }
    }

    private fun InputStream.toChunkedFlow(fileSizeInBytes: Long, chunkSize: Int): Flow<ByteArray> =
        flow {
            if (fileSizeInBytes == chunkSize.toLong()) {
                emit(this@toChunkedFlow.readBytes())
            } else {
                val buffer = ByteArray(chunkSize)
                var bytesRead = read(buffer)
                while (bytesRead >= 0) {
                    emit(buffer.copyOf(bytesRead))
                    bytesRead = read(buffer)
                }
            }
        }.flowOn(dispatchers.io())

    private fun MessageDigest.digestAsString(): String {
        return this.digest().joinToString("") { byte -> "%02x".format(byte) }
    }
}
