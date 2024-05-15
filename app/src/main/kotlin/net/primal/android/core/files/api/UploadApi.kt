package net.primal.android.core.files.api

import net.primal.android.nostr.model.NostrEvent

interface UploadApi {

    suspend fun uploadChunk(chunkEvent: NostrEvent)

    suspend fun completeUpload(completeEvent: NostrEvent): String

    suspend fun cancelUpload(cancelEvent: NostrEvent)
}
