package net.primal.networking.primal.upload.api


import net.primal.networking.model.NostrEvent

interface UploadApi {

    
    suspend fun uploadChunk(chunkEvent: NostrEvent)

    
    suspend fun completeUpload(completeEvent: NostrEvent): String

    
    suspend fun cancelUpload(cancelEvent: NostrEvent)
}
