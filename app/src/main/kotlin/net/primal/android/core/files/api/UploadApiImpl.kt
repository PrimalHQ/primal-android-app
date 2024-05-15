package net.primal.android.core.files.api

import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.core.files.model.UploadChunkRequest
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.networking.di.PrimalUploadApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalQueryResult
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import timber.log.Timber

class UploadApiImpl @Inject constructor(
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
                    primalVerb = PrimalVerb.UPLOAD_CHUNK,
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
