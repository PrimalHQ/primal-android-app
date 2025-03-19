package net.primal.data.remote.api.upload

import net.primal.domain.nostr.NostrEvent

internal interface UploadApi {

    suspend fun uploadChunk(chunkEvent: NostrEvent)

    suspend fun completeUpload(completeEvent: NostrEvent): String

    suspend fun cancelUpload(cancelEvent: NostrEvent)
}
