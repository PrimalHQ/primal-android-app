package net.primal.android.events.api

import javax.inject.Inject
import net.primal.android.events.api.model.EventActionsRequestBody
import net.primal.android.events.api.model.EventActionsResponse
import net.primal.android.events.api.model.EventZapsRequestBody
import net.primal.android.events.api.model.EventZapsResponse
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.domain.nostr.NostrEventKind

class EventStatsApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : EventStatsApi {

    override suspend fun getEventZaps(body: EventZapsRequestBody): EventZapsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.EVENT_ZAPS.id,
                optionsJson = CommonJson.encodeToString(body),
            ),
        )

        return EventZapsResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                CommonJson.decodeFromStringOrNull(it?.content)
            },
            profiles = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getEventActions(body: EventActionsRequestBody): EventActionsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.EVENT_ACTIONS.id,
                optionsJson = CommonJson.encodeToString(body),
            ),
        )

        return EventActionsResponse(
            profiles = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            userFollowersCount = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            actions = queryResult.filterNostrEvents(NostrEventKind.valueOf(body.kind)),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }
}
