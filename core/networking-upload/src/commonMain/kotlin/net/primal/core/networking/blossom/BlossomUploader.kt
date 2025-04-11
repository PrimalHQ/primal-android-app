package net.primal.core.networking.blossom

import okio.BufferedSource

interface BlossomUploader {
    suspend fun uploadBlob(
        userId: String,
        blossomUrl: String,
        inputStream: () -> BufferedSource,
        onProgress: ((uploadedBytes: Int, totalBytes: Int) -> Unit)? = null,
    ): BlobDescriptor
}
