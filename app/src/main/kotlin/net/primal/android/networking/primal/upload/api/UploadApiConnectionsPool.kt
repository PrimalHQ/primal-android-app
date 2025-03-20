package net.primal.android.networking.primal.upload.api

import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.channels.Channel
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.primal.upload.api.model.UploadChunkRequest
import net.primal.core.networking.factory.PrimalApiClientFactory
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalQueryResult
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.PrimalServerType
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import timber.log.Timber

class UploadApiConnectionsPool : UploadApi {

    companion object {
        const val POOL_SIZE = 5
    }

    private val channel = Channel<PrimalApiClient>(Channel.UNLIMITED)

    init {
        repeat(times = POOL_SIZE) {
            channel.trySend(
                PrimalApiClientFactory.create(PrimalServerType.Upload),
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
                    primalVerb = net.primal.data.remote.PrimalVerb.UPLOAD_CHUNK.id,
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
                    primalVerb = net.primal.data.remote.PrimalVerb.UPLOAD_COMPLETE.id,
                    optionsJson = NostrJsonEncodeDefaults.encodeToString(
                        UploadChunkRequest(event = eventBlock()),
                    ),
                ),
            )
        }

    private suspend fun uploadOrThrow(block: suspend () -> PrimalQueryResult) =
        try {
            block()
        } catch (error: WssException) {
            Timber.w(error)
            throw UnsuccessfulFileUpload(cause = error)
        } catch (error: IOException) {
            Timber.w(error)
            throw UnsuccessfulFileUpload(cause = error)
        } catch (error: UnknownHostException) {
            Timber.w(error)
            throw UnsuccessfulFileUpload(cause = error)
        }
}
