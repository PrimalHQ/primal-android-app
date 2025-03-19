package net.primal.data.remote.uploader

import io.github.aakira.napier.Napier
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalQueryResult
import net.primal.core.networking.sockets.errors.WssException
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.upload.UploadApi
import net.primal.data.remote.api.upload.model.UploadChunkRequest
import net.primal.data.repository.upload.UnsuccessfulFileUpload
import net.primal.data.serialization.NostrJsonEncodeDefaults
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

internal class UploadApiSingleConnection(
    private val primalUploadClient: PrimalApiClient,
) : UploadApi {

    override suspend fun uploadChunk(chunkEvent: NostrEvent) {
        uploadChunkOrThrow { chunkEvent }
    }

    override suspend fun completeUpload(completeEvent: NostrEvent): String {
        val result = uploadCompleteOrThrow { completeEvent }
        return result.findPrimalEvent(NostrEventKind.PrimalUploadResponse)
            ?.content ?: throw WssException("Remote url not found.")
    }

    override suspend fun cancelUpload(cancelEvent: NostrEvent) {
        uploadChunkOrThrow { cancelEvent }
    }

    private suspend fun uploadChunkOrThrow(eventBlock: () -> NostrEvent) =
        uploadOrThrow {
            primalUploadClient.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.UPLOAD_CHUNK.id,
                    optionsJson = NostrJsonEncodeDefaults.encodeToString(
                        UploadChunkRequest(event = eventBlock()),
                    ),
                ),
            )
        }

    private suspend fun uploadCompleteOrThrow(eventBlock: () -> NostrEvent) =
        uploadOrThrow {
            primalUploadClient.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.UPLOAD_COMPLETE.id,
                    optionsJson = NostrJsonEncodeDefaults.encodeToString(
                        UploadChunkRequest(event = eventBlock()),
                    ),
                ),
            )
        }

    private suspend fun uploadOrThrow(block: suspend () -> PrimalQueryResult) =
        try {
            block()
        } catch (error: Exception) {
            Napier.w(error) { "failed to upload " }
            throw UnsuccessfulFileUpload(cause = error)
        }
}
