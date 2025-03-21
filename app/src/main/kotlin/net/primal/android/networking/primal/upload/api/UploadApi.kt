package net.primal.android.networking.primal.upload.api

import net.primal.domain.nostr.NostrEvent

interface UploadApi {

    suspend fun uploadChunk(chunkEvent: NostrEvent)

    suspend fun completeUpload(completeEvent: NostrEvent): String

    suspend fun cancelUpload(cancelEvent: NostrEvent)
}
