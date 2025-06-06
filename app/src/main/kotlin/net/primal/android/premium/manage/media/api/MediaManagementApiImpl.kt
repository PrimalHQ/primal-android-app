package net.primal.android.premium.manage.media.api

import javax.inject.Inject
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.manage.media.api.model.MediaStorageStats
import net.primal.android.premium.manage.media.api.model.MediaUploadsRequestBody
import net.primal.android.premium.manage.media.api.model.MediaUploadsResponse
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.model.AppSpecificDataRequest
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow

class MediaManagementApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : MediaManagementApi {

    override suspend fun getMediaStats(userId: String): MediaStorageStats {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEDIA_MANAGEMENT_STATS.id,
                optionsJson = AppSpecificDataRequest(
                    eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                        userId = userId,
                        content = "",
                    ).unwrapOrThrow(),
                ).encodeToJsonString(),
            ),
        )

        return queryResult.findPrimalEvent(NostrEventKind.PrimalUserMediaStorageStats)
            .takeContentOrNull<MediaStorageStats>()
            ?: throw NetworkException("Missing event or invalid content.")
    }

    override suspend fun getMediaUploads(userId: String): MediaUploadsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEDIA_MANAGEMENT_UPLOADS.id,
                optionsJson = MediaUploadsRequestBody(
                    eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(userId = userId, content = "")
                        .unwrapOrThrow(),
                    since = 0,
                    limit = 1_000,
                ).encodeToJsonString(),
            ),
        )

        return MediaUploadsResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                it?.content.decodeFromJsonStringOrNull()
            },
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            uploadInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalUserUploadInfo),
        )
    }

    override suspend fun deleteMedia(userId: String, mediaUrl: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEDIA_MANAGEMENT_DELETE.id,
                optionsJson = AppSpecificDataRequest(
                    eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                        userId = userId,
                        content = "{\"url\":\"$mediaUrl\"}",
                    ).unwrapOrThrow(),
                ).encodeToJsonString(),
            ),
        )
    }
}
