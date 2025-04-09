package net.primal.android.networking.primal.upload.api.model

import android.util.Base64
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag

fun chunkUploadRequest(
    userId: String,
    uploadId: String,
    fileSizeInBytes: Long,
    offsetInBytes: Int,
    data: ByteArray,
): NostrUnsignedEvent {
    return NostrUnsignedEvent(
        pubKey = userId,
        kind = NostrEventKind.PrimalChunkedUploadRequest.value,
        tags = listOf(userId.asPubkeyTag()),
        content = UploadChunkContent(
            uploadId = uploadId,
            fileLength = fileSizeInBytes,
            offset = offsetInBytes,
            base64Data = Base64.encodeToString(data, Base64.NO_WRAP).asOctetStream(),
        ).encodeToJsonString(),
    )
}

fun completeUploadRequest(
    userId: String,
    uploadId: String,
    fileSizeInBytes: Long,
    hash: String,
): NostrUnsignedEvent {
    return NostrUnsignedEvent(
        pubKey = userId,
        kind = NostrEventKind.PrimalChunkedUploadRequest.value,
        tags = listOf(userId.asPubkeyTag()),
        content = UploadCompleteContent(
            uploadId = uploadId,
            fileLength = fileSizeInBytes,
            hash = hash,
        ).encodeToJsonString(),
    )
}

fun cancelUploadRequest(userId: String, uploadId: String): NostrUnsignedEvent {
    return NostrUnsignedEvent(
        pubKey = userId,
        kind = NostrEventKind.PrimalChunkedUploadRequest.value,
        tags = listOf(userId.asPubkeyTag()),
        content = UploadCancelContent(uploadId = uploadId).encodeToJsonString(),
    )
}

private fun String.asOctetStream(): String = "data:application/octet-stream;base64,,$this"
