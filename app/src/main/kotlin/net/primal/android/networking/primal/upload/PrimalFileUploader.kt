package net.primal.android.networking.primal.upload

import android.content.ContentResolver
import android.net.Uri
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withContext
import net.primal.android.BuildConfig
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.crypto.NostrKeyPair
import net.primal.android.crypto.hexToNsecHrp
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.primal.upload.api.UploadApi
import net.primal.android.networking.primal.upload.api.UploadApiConnectionsPool
import net.primal.android.networking.primal.upload.api.model.cancelUploadRequest
import net.primal.android.networking.primal.upload.api.model.chunkUploadRequest
import net.primal.android.networking.primal.upload.api.model.completeUploadRequest
import net.primal.android.nostr.notary.NostrNotary
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.android.nostr.notary.signOrThrow
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.upload.UploadResult
import net.primal.domain.upload.UploadStatus

class PrimalFileUploader @Inject constructor(
    private val contentResolver: ContentResolver,
    private val dispatchers: CoroutineDispatcherProvider,
    private val uploadApi: UploadApi,
    private val nostrNotary: NostrNotary,
) {

    companion object {
        private const val KB = 1024
        private const val MB = 1024 * KB

        fun generateRandomUploadId(): String {
            val uploadFriendlyVersionName = BuildConfig.VERSION_NAME.replace(".", "_")
            return "${UUID.randomUUID()}-${UserAgentProvider.APP_NAME}-$uploadFriendlyVersionName"
        }
    }

    private val uploadsMap = mutableMapOf<String, UploadStatus>()

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadFile(
        uri: Uri,
        keyPair: NostrKeyPair,
        uploadId: String = generateRandomUploadId(),
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult {
        val userId = keyPair.pubKey
        return uploadFileOrThrow(
            uri = uri,
            userId = userId,
            uploadId = uploadId,
            signNostrEvent = { it.signOrThrow(keyPair.privateKey.hexToNsecHrp()) },
            onProgress = { uploadedBytes, totalBytes ->
                onProgress?.invoke(uploadedBytes, totalBytes)
            },
        )
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadFile(
        uri: Uri,
        userId: String,
        uploadId: String = generateRandomUploadId(),
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): UploadResult {
        return uploadFileOrThrow(
            uri = uri,
            userId = userId,
            uploadId = uploadId,
            signNostrEvent = { nostrNotary.signNostrEvent(userId = userId, event = it) },
            onProgress = { uploadedBytes, totalBytes ->
                onProgress?.invoke(uploadedBytes, totalBytes)
            },
        )
    }

    suspend fun cancelUpload(keyPair: NostrKeyPair, uploadId: String) {
        val userId = keyPair.pubKey
        return cancelUploadOrThrow(
            userId = userId,
            uploadId = uploadId,
            signNostrEvent = { it.signOrThrow(keyPair.privateKey.hexToNsecHrp()) },
        )
    }

    suspend fun cancelUpload(userId: String, uploadId: String) {
        cancelUploadOrThrow(
            userId = userId,
            uploadId = uploadId,
            signNostrEvent = { nostrNotary.signNostrEvent(userId = userId, event = it) },
        )
    }

    private suspend fun cancelUploadOrThrow(
        userId: String,
        uploadId: String = generateRandomUploadId(),
        signNostrEvent: (NostrUnsignedEvent) -> NostrEvent,
    ) {
        if (uploadsMap[uploadId] !is UploadStatus.UploadCompleted) {
            uploadApi.cancelUpload(
                signNostrEvent(
                    cancelUploadRequest(
                        userId = userId,
                        uploadId = uploadId.toString(),
                    ),
                ),
            )
        }
    }

    private suspend fun uploadFileOrThrow(
        uri: Uri,
        userId: String,
        uploadId: String = generateRandomUploadId(),
        signNostrEvent: (NostrUnsignedEvent) -> NostrEvent,
        onProgress: (uploadedBytes: Int, totalBytes: Int) -> Unit,
    ): UploadResult {
        val fileDigest = MessageDigest.getInstance("SHA-256")
        return withContext(dispatchers.io()) {
            val uploadResultTask = runCatching {
                uploadsMap[uploadId] = UploadStatus.Uploading
                val fileSizeInBytes = uri.readFileSizeInBytes()
                val chunkSize = calculateChunkSize(fileSizeInBytes)
                var uploadedChunks = 0
                onProgress(0, fileSizeInBytes.toInt())
                contentResolver.openInputStream(uri).use { inputStream ->
                    if (inputStream == null) throw FileNotFoundException()
                    inputStream.toChunkedFlow(fileSizeInBytes = fileSizeInBytes, chunkSize = chunkSize)
                        .withIndex()
                        .map {
                            val offset = it.index * chunkSize
                            fileDigest.update(it.value, 0, it.value.size)
                            async(context = dispatchers.io()) {
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
                        .buffer(capacity = UploadApiConnectionsPool.POOL_SIZE)
                        .collect { deferred ->
                            deferred.await()
                            val uploadedBytes = ++uploadedChunks * chunkSize
                            onProgress(uploadedBytes, fileSizeInBytes.toInt())
                        }
                }

                val hash = fileDigest.digestAsString()
                val remoteUrl = uploadApi.completeUpload(
                    signNostrEvent(
                        completeUploadRequest(
                            userId = userId,
                            uploadId = uploadId,
                            fileSizeInBytes = fileSizeInBytes,
                            hash = hash,
                        ),
                    ),
                )
                uploadsMap[uploadId] = UploadStatus.UploadCompleted
                UploadResult(
                    remoteUrl = remoteUrl,
                    originalFileSize = fileSizeInBytes.toInt(),
                    originalHash = hash,
                )
            }

            val uploadResult = uploadResultTask.getOrNull()
            if (uploadResult != null) {
                uploadResult
            } else {
                uploadsMap[uploadId] = UploadStatus.UploadFailed
                throw UnsuccessfulFileUpload(cause = uploadResultTask.exceptionOrNull())
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
