package net.primal.data.remote.api.premium

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.CommonJsonEncodeDefaults
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.broadcast.model.ContentEventKindCount
import net.primal.data.remote.api.broadcast.model.StartContentBroadcastRequest
import net.primal.data.remote.model.AppSpecificDataRequest
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.common.util.takeContentOrNull
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.premium.BroadcastingStatus

class PremiumBroadcastApiImpl(
    private val primalApiClient: PrimalApiClient,
) : PremiumBroadcastApi {

    override suspend fun getContentStats(userId: String, signedAppSpecificDataNostrEvent: NostrEvent): Map<Int, Long> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_CONTENT_STATS.id,
                optionsJson = CommonJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(eventFromUser = signedAppSpecificDataNostrEvent),
                ),
            ),
        )

        val event = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalContentBroadcastStats)
        val counts = event.takeContentOrNull<List<ContentEventKindCount>>()
        return counts?.associate { it.kind to it.count } ?: throw NetworkException("Missing or invalid content")
    }

    override suspend fun startContentRebroadcast(
        userId: String,
        kinds: List<Int>?,
        signedAppSpecificDataNostrEvent: NostrEvent,
    ) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_START.id,
                optionsJson = StartContentBroadcastRequest(
                    eventFromUser = signedAppSpecificDataNostrEvent,
                    kinds = kinds,
                ).encodeToJsonString(),
            ),
        )
    }

    override suspend fun cancelContentRebroadcast(userId: String, signedAppSpecificDataNostrEvent: NostrEvent) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_CANCEL.id,
                optionsJson = CommonJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = signedAppSpecificDataNostrEvent,
                    ),
                ),
            ),
        )
    }

    override suspend fun getContentRebroadcastStatus(
        userId: String,
        signedAppSpecificDataNostrEvent: NostrEvent,
    ): BroadcastingStatus {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MEMBERSHIP_CONTENT_BROADCAST_STATUS.id,
                optionsJson = CommonJsonEncodeDefaults.encodeToString(
                    AppSpecificDataRequest(
                        eventFromUser = signedAppSpecificDataNostrEvent,
                    ),
                ),
            ),
        )

        return queryResult.findPrimalEvent(NostrEventKind.PrimalContentBroadcastStatus)
            ?.takeContentOrNull<BroadcastingStatus>()
            ?: throw NetworkException("Missing or invalid content")
    }
}
