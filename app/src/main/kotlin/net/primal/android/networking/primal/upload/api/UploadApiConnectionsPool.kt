package net.primal.android.networking.primal.upload.api

import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import net.primal.android.config.AppConfigHandler
import net.primal.android.config.AppConfigProvider
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalQueryResult
import net.primal.android.networking.primal.PrimalServerType
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.primal.upload.UnsuccessfulFileUpload
import net.primal.android.networking.primal.upload.api.model.UploadChunkRequest
import net.primal.android.networking.sockets.errors.WssException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import okhttp3.OkHttpClient
import timber.log.Timber

class UploadApiConnectionsPool @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val okHttpClient: OkHttpClient,
    private val appConfigProvider: AppConfigProvider,
    private val appConfigHandler: AppConfigHandler,
) : UploadApi {

    companion object {
        const val POOL_SIZE = 5
    }

    private val channel = Channel<PrimalApiClient>(Channel.UNLIMITED)

    init {
        repeat(times = POOL_SIZE) {
            channel.trySend(
                PrimalApiClient(
                    dispatcherProvider = dispatcherProvider,
                    okHttpClient = okHttpClient,
                    serverType = PrimalServerType.Upload,
                    appConfigProvider = appConfigProvider,
                    appConfigHandler = appConfigHandler,
                ),
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
