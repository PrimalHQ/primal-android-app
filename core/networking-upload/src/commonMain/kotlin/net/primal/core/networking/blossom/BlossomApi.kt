package net.primal.core.networking.blossom

import kotlin.coroutines.cancellation.CancellationException
import okio.BufferedSource

interface BlossomApi {

    @Throws(
        UploadRequirementException::class,
        CancellationException::class,
    )
    suspend fun headUpload(authorization: String, fileMetadata: FileMetadata)

    @Throws(
        BlossomUploadException::class,
        CancellationException::class,
    )
    suspend fun putUpload(
        authorization: String,
        fileMetadata: FileMetadata,
        openBufferedSource: () -> BufferedSource,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): BlobDescriptor

    @Throws(
        UploadRequirementException::class,
        CancellationException::class,
    )
    suspend fun headMedia(authorization: String, fileMetadata: FileMetadata)

    @Throws(
        BlossomUploadException::class,
        CancellationException::class,
    )
    suspend fun putMedia(
        authorization: String,
        fileMetadata: FileMetadata,
        openBufferedSource: () -> BufferedSource,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): BlobDescriptor

    @Throws(
        BlossomMirrorException::class,
        CancellationException::class,
    )
    suspend fun putMirror(authorization: String, fileUrl: String): BlobDescriptor
}
