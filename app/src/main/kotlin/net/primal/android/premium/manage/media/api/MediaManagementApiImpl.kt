package net.primal.android.premium.manage.media.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.manage.media.api.model.MediaStorageStats
import net.primal.android.premium.manage.media.api.model.MediaUploadsResponse
import net.primal.android.settings.api.model.AppSpecificDataRequest

class MediaManagementApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : MediaManagementApi {

    override suspend fun getMediaStats(userId: String): MediaStorageStats {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEDIA_MANAGEMENT_STATS,
                optionsJson = NostrJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "",
                        ),
                    ),
                ),
            ),
        )

        return queryResult.findPrimalEvent(NostrEventKind.PrimalUserMediaStorageStats)
            .takeContentOrNull<MediaStorageStats>()
            ?: throw WssException("Missing event or invalid content.")
    }

    override suspend fun getMediaUploads(userId: String): MediaUploadsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEDIA_MANAGEMENT_UPLOADS,
                optionsJson = NostrJson.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{\"since\": 0, \"limit\": 1000}",
                        ),
                    ),
                ),
            ),
        )

        return MediaUploadsResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            uploadInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalUserUploadInfo),
        )
    }

    override suspend fun deleteMedia(userId: String) {
    }
}
