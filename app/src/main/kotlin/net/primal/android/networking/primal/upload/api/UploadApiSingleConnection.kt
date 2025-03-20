package net.primal.android.networking.primal.upload.api

import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.networking.di.PrimalUploadApiClient
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.primal.upload.api.model.UploadChunkRequest
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalQueryResult
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import timber.log.Timber

class UploadApiSingleConnection @Inject constructor(
    @PrimalUploadApiClient private val primalUploadClient: PrimalApiClient,
) : UploadApi {

    override suspend fun uploadChunk(chunkEvent: NostrEvent) {
        uploadChunkOrThrow { chunkEvent }
    }

    override suspend fun completeUpload(completeEvent: NostrEvent): String {
        val result = uploadCompleteOrThrow { completeEvent }
        return result
            .findPrimalEvent(NostrEventKind.PrimalUploadResponse)
            ?.content ?: throw WssException("Remote url not found.")
    }

    override suspend fun cancelUpload(cancelEvent: NostrEvent) {
        uploadChunkOrThrow { cancelEvent }
    }

    private suspend fun uploadChunkOrThrow(eventBlock: () -> NostrEvent) =
        uploadOrThrow {
            primalUploadClient.query(
                message = PrimalCacheFilter(
                    primalVerb = net.primal.data.remote.PrimalVerb.UPLOAD_CHUNK.id,
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
