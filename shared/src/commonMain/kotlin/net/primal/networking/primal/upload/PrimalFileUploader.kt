package net.primal.networking.primal.upload

// TODO File upload has android dependencies
internal class PrimalFileUploader(
//    private val contentResolver: ContentResolver,
//    private val dispatchers: DispatcherProvider,
//    private val uploadApi: UploadApi,
//    private val nostrNotary: NostrNotary,
) {

//    companion object {
//        private const val KB = 1024
//        private const val MB = 1024 * KB
//
//        fun generateRandomUploadId(): String {
//            val uploadFriendlyVersionName = BuildConfig.VERSION_NAME.replace(".", "_")
//            return "${UUID.randomUUID()}-${UserAgentProvider.APP_NAME}-$uploadFriendlyVersionName"
//        }
//    }
//
//    private val uploadsMap = mutableMapOf<String, UploadStatus>()
//
//    @Throws(UnsuccessfulFileUpload::class)
//    suspend fun uploadFile(
//        uri: Uri,
//        keyPair: NostrKeyPair,
//        uploadId: String = generateRandomUploadId(),
//        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
//    ): UploadResult {
//        val userId = keyPair.pubKey
//        return uploadFileOrThrow(
//            uri = uri,
//            userId = userId,
//            uploadId = uploadId,
//            signNostrEvent = { it.signOrThrow(keyPair.privateKey.hexToNsecHrp()) },
//            onProgress = { uploadedBytes, totalBytes ->
//                onProgress?.invoke(uploadedBytes, totalBytes)
//            },
//        )
//    }
//
//    @Throws(UnsuccessfulFileUpload::class)
//    suspend fun uploadFile(
//        uri: Uri,
//        userId: String,
//        uploadId: String = generateRandomUploadId(),
//        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
//    ): UploadResult {
//        return uploadFileOrThrow(
//            uri = uri,
//            userId = userId,
//            uploadId = uploadId,
//            signNostrEvent = { nostrNotary.signNostrEvent(userId = userId, event = it) },
//            onProgress = { uploadedBytes, totalBytes ->
//                onProgress?.invoke(uploadedBytes, totalBytes)
//            },
//        )
//    }
//
//    suspend fun cancelUpload(keyPair: NostrKeyPair, uploadId: String) {
//        val userId = keyPair.pubKey
//        return cancelUploadOrThrow(
//            userId = userId,
//            uploadId = uploadId,
//            signNostrEvent = { it.signOrThrow(keyPair.privateKey.hexToNsecHrp()) },
//        )
//    }
//
//    suspend fun cancelUpload(userId: String, uploadId: String) {
//        cancelUploadOrThrow(
//            userId = userId,
//            uploadId = uploadId,
//            signNostrEvent = { nostrNotary.signNostrEvent(userId = userId, event = it) },
//        )
//    }
//
//    private suspend fun cancelUploadOrThrow(
//        userId: String,
//        uploadId: String = generateRandomUploadId(),
//        signNostrEvent: (NostrUnsignedEvent) -> NostrEvent,
//    ) {
//        if (uploadsMap[uploadId] !is UploadStatus.UploadCompleted) {
//            uploadApi.cancelUpload(
//                signNostrEvent(
//                    cancelUploadRequest(
//                        userId = userId,
//                        uploadId = uploadId.toString(),
//                    ),
//                ),
//            )
//        }
//    }
//
//    private suspend fun uploadFileOrThrow(
//        uri: Uri,
//        userId: String,
//        uploadId: String = generateRandomUploadId(),
//        signNostrEvent: (NostrUnsignedEvent) -> NostrEvent,
//        onProgress: (uploadedBytes: Int, totalBytes: Int) -> Unit,
//    ): UploadResult {
//        val fileDigest = MessageDigest.getInstance("SHA-256")
//        return withContext(dispatchers.io()) {
//            val uploadResultTask = runCatching {
//                uploadsMap[uploadId] = UploadStatus.Uploading
//                val fileSizeInBytes = uri.readFileSizeInBytes()
//                val chunkSize = calculateChunkSize(fileSizeInBytes)
//                var uploadedChunks = 0
//                onProgress(0, fileSizeInBytes.toInt())
//                contentResolver.openInputStream(uri).use { inputStream ->
//                    if (inputStream == null) throw FileNotFoundException()
//                    inputStream.toChunkedFlow(fileSizeInBytes = fileSizeInBytes, chunkSize = chunkSize)
//                        .withIndex()
//                        .map {
//                            val offset = it.index * chunkSize
//                            fileDigest.update(it.value, 0, it.value.size)
//                            async(context = dispatchers.io()) {
//                                uploadApi.uploadChunk(
//                                    signNostrEvent(
//                                        chunkUploadRequest(
//                                            userId = userId,
//                                            uploadId = uploadId,
//                                            fileSizeInBytes = fileSizeInBytes,
//                                            offsetInBytes = offset,
//                                            data = it.value,
//                                        ),
//                                    ),
//                                )
//                            }
//                        }
//                        .buffer(capacity = UploadApiConnectionsPool.POOL_SIZE)
//                        .collect { deferred ->
//                            deferred.await()
//                            val uploadedBytes = ++uploadedChunks * chunkSize
//                            onProgress(uploadedBytes, fileSizeInBytes.toInt())
//                        }
//                }
//
//                val hash = fileDigest.digestAsString()
//                val remoteUrl = uploadApi.completeUpload(
//                    signNostrEvent(
//                        completeUploadRequest(
//                            userId = userId,
//                            uploadId = uploadId,
//                            fileSizeInBytes = fileSizeInBytes,
//                            hash = hash,
//                        ),
//                    ),
//                )
//                uploadsMap[uploadId] = UploadStatus.UploadCompleted
//                UploadResult(
//                    remoteUrl = remoteUrl,
//                    originalFileSize = fileSizeInBytes.toInt(),
//                    originalHash = hash,
//                )
//            }
//
//            val uploadResult = uploadResultTask.getOrNull()
//            if (uploadResult != null) {
//                uploadResult
//            } else {
//                uploadsMap[uploadId] = UploadStatus.UploadFailed
//                throw UnsuccessfulFileUpload(cause = uploadResultTask.exceptionOrNull())
//            }
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun Uri.readFileSizeInBytes(): Long {
//        contentResolver.openFileDescriptor(this, "r")?.use {
//            FileInputStream(it.fileDescriptor).use { inputStream ->
//                return inputStream.channel.size()
//            }
//        }
//        throw FileNotFoundException()
//    }
//
//    @Suppress("MagicNumber")
//    private fun calculateChunkSize(fileSizeInBytes: Long): Int {
//        return when (fileSizeInBytes) {
//            in 0 until MB / 2 -> fileSizeInBytes.toInt()
//            in MB / 2 until 2 * MB -> MB / 4
//            in 2 * MB until 8 * MB -> MB / 2
//            else -> MB
//        }
//    }
//
//    private fun InputStream.toChunkedFlow(fileSizeInBytes: Long, chunkSize: Int): Flow<ByteArray> =
//        flow {
//            if (fileSizeInBytes == chunkSize.toLong()) {
//                emit(this@toChunkedFlow.readBytes())
//            } else {
//                val buffer = ByteArray(chunkSize)
//                var bytesRead = read(buffer)
//                while (bytesRead >= 0) {
//                    emit(buffer.copyOf(bytesRead))
//                    bytesRead = read(buffer)
//                }
//            }
//        }.flowOn(dispatchers.io())
//
//    private fun MessageDigest.digestAsString(): String {
//        return this.digest().joinToString("") { byte -> "%02x".format(byte) }
//    }
}
