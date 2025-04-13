package net.primal.core.networking.blossom

import okio.BufferedSource

interface BlossomApi {

    suspend fun headUpload(authorization: String, fileMetadata: FileMetadata)

    suspend fun putUpload(
        authorization: String,
        fileMetadata: FileMetadata,
        openBufferedSource: () -> BufferedSource,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): BlobDescriptor

    suspend fun headMedia(authorization: String, fileMetadata: FileMetadata)

    suspend fun putMedia(
        authorization: String,
        fileMetadata: FileMetadata,
        openBufferedSource: () -> BufferedSource,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): BlobDescriptor

    suspend fun putMirror(authorization: String, fileUrl: String): BlobDescriptor
}
