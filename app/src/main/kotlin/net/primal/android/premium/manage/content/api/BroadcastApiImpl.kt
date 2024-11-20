package net.primal.android.premium.manage.content.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.manage.content.api.model.BroadcastingStatus
import net.primal.android.premium.manage.content.api.model.ContentEventKindCount
import net.primal.android.premium.manage.content.api.model.StartContentBroadcastRequest
import net.primal.android.settings.api.model.AppSpecificDataRequest

class BroadcastApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalCacheApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : BroadcastApi {

    override suspend fun getContentStats(userId: String): Map<Int, Long> {
        val queryResult = primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_CONTENT_STATS,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{}",
                        ),
                    ),
                ),
            ),
        )

        val event = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalContentBroadcastStats)
        val counts = event.takeContentOrNull<List<ContentEventKindCount>>()
        return counts?.associate { it.kind to it.count } ?: throw WssException("Missing or invalid content")
    }

    override suspend fun startContentRebroadcast(userId: String, kinds: List<Int>?) {
        primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_START,
                optionsJson = NostrJson.encodeToString(
                    StartContentBroadcastRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{}",
                        ),
                        kinds = kinds,
                    ),
                ),
            ),
        )
    }

    override suspend fun cancelContentRebroadcast(userId: String) {
        primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_CANCEL,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{}",
                        ),
                    ),
                ),
            ),
        )
    }

    override suspend fun getContentRebroadcastStatus(userId: String): BroadcastingStatus {
        val queryResult = primalCacheApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_STATUS,
                optionsJson = NostrJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = nostrNotary.signAppSpecificDataNostrEvent(
                            userId = userId,
                            content = "{}",
                        ),
                    ),
                ),
            ),
        )

        return queryResult.findPrimalEvent(NostrEventKind.PrimalContentBroadcastStatus)
            ?.takeContentOrNull<BroadcastingStatus>()
            ?: throw WssException("Missing or invalid content")
    }
}
