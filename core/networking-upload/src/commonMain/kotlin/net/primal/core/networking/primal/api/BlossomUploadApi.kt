package net.primal.core.networking.primal.api

import net.primal.core.networking.primal.api.model.BlobDescriptor

interface BlossomUploadApi {
    suspend fun uploadBlob(
        data: ByteArray,
        mimeType: String?,
        authorization: String?,
    ): BlobDescriptor
}
