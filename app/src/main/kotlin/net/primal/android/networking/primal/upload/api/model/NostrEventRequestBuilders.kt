package net.primal.android.networking.primal.upload.api.model

import android.util.Base64
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.domain.nostr.NostrEventKind

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
        content = NostrJson.encodeToString(
            UploadChunkContent(
                uploadId = uploadId,
                fileLength = fileSizeInBytes,
                offset = offsetInBytes,
                base64Data = Base64.encodeToString(data, Base64.NO_WRAP).asOctetStream(),
            ),
        ),
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
        content = NostrJson.encodeToString(
            UploadCompleteContent(
                uploadId = uploadId,
                fileLength = fileSizeInBytes,
                hash = hash,
            ),
        ),
    )
}

fun cancelUploadRequest(userId: String, uploadId: String): NostrUnsignedEvent {
    return NostrUnsignedEvent(
        pubKey = userId,
        kind = NostrEventKind.PrimalChunkedUploadRequest.value,
        tags = listOf(userId.asPubkeyTag()),
        content = NostrJson.encodeToString(UploadCancelContent(uploadId = uploadId)),
    )
}

private fun String.asOctetStream(): String = "data:application/octet-stream;base64,,$this"
