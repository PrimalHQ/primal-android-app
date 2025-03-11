package net.primal.networking.primal.upload.api

import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import net.primal.networking.model.NostrEvent
import net.primal.networking.model.NostrEventKind
import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.primal.PrimalApiClientFactory
import net.primal.networking.primal.PrimalCacheFilter
import net.primal.networking.primal.PrimalQueryResult
import net.primal.networking.primal.PrimalServerType
import net.primal.networking.primal.PrimalVerb
import net.primal.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.networking.primal.upload.api.model.UploadChunkRequest
import net.primal.networking.sockets.errors.WssException
import net.primal.serialization.json.NostrJsonEncodeDefaults

internal class UploadApiConnectionsPool : UploadApi {

    companion object {
        const val POOL_SIZE = 5
    }

    private val channel = Channel<PrimalApiClient>(Channel.UNLIMITED)

    init {
        repeat(times = POOL_SIZE) {
            channel.trySend(
                PrimalApiClientFactory.create(serverType = PrimalServerType.Upload),
            )
        }
    }

    override suspend fun uploadChunk(chunkEvent: NostrEvent) {
        val primalApiClient = channel.receive()
        try {
            primalApiClient.uploadChunkOrThrow { chunkEvent }
        } finally {
            channel.send(primalApiClient)
        }
    }

    override suspend fun completeUpload(completeEvent: NostrEvent): String {
        val primalApiClient = channel.receive()

        val result = try {
            primalApiClient.uploadCompleteOrThrow { completeEvent }
        } finally {
            channel.send(primalApiClient)
        }

        return result
            .findPrimalEvent(NostrEventKind.PrimalUploadResponse)
            ?.content ?: throw WssException("Remote url not found.")
    }

    override suspend fun cancelUpload(cancelEvent: NostrEvent) {
        val primalApiClient = channel.receive()
        try {
            primalApiClient.uploadChunkOrThrow { cancelEvent }
        } finally {
            channel.send(primalApiClient)
        }
    }

    private suspend fun PrimalApiClient.uploadChunkOrThrow(eventBlock: () -> NostrEvent) =
        uploadOrThrow {
            this.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.UPLOAD_CHUNK,
                    optionsJson = NostrJsonEncodeDefaults.encodeToString(
                        UploadChunkRequest(event = eventBlock()),
                    ),
                ),
            )
        }

    private suspend fun PrimalApiClient.uploadCompleteOrThrow(eventBlock: () -> NostrEvent) =
        uploadOrThrow {
            this.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.UPLOAD_COMPLETE,
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
            Napier.w(error) { "failed to upload" }
            throw UnsuccessfulFileUpload(cause = error)
        }
}
