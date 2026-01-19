package net.primal.data.remote.api.events

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.primal.PrimalQueryResult
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.events.model.EventActionsRequestBody
import net.primal.data.remote.api.events.model.EventActionsResponse
import net.primal.data.remote.api.events.model.EventZapsRequestBody
import net.primal.data.remote.api.events.model.EventZapsResponse
import net.primal.data.remote.api.events.model.EventsNip46Request
import net.primal.data.remote.api.events.model.EventsNip46Response
import net.primal.data.remote.api.events.model.InvoicesToZapReceiptsRequest
import net.primal.data.remote.api.events.model.InvoicesToZapReceiptsResponse
import net.primal.data.remote.api.events.model.ReplaceableEventRequest
import net.primal.data.remote.api.events.model.ReplaceableEventResponse
import net.primal.data.remote.api.events.model.ReplaceableEventsRequest
import net.primal.domain.nostr.NostrEventKind

internal class EventStatsApiImpl(
    private val primalApiClient: PrimalApiClient,
) : EventStatsApi {

    override suspend fun getEventZaps(body: EventZapsRequestBody): EventZapsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.EVENT_ZAPS.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return EventZapsResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                it?.content.decodeFromJsonStringOrNull()
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
                primalVerb = PrimalVerb.EVENT_ACTIONS.id,
                optionsJson = body.encodeToJsonString(),
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

    override suspend fun getReplaceableEvent(body: ReplaceableEventRequest) =
        runCatching {
            val queryResult = primalApiClient.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.PARAMETRIZED_REPLACEABLE_EVENT.id,
                    optionsJson = body.encodeToJsonString(),
                ),
            )

            buildReplaceableEventResponse(queryResult = queryResult)
        }

    override suspend fun getReplaceableEvents(body: ReplaceableEventsRequest) =
        runCatching {
            val queryResult = primalApiClient.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.PARAMETRIZED_REPLACEABLE_EVENTS.id,
                    optionsJson = body.encodeToJsonString(),
                ),
            )

            buildReplaceableEventResponse(queryResult = queryResult)
        }

    private fun buildReplaceableEventResponse(queryResult: PrimalQueryResult) =
        ReplaceableEventResponse(
            metadata = queryResult.filterNostrEvents(kind = NostrEventKind.Metadata),
            articles = queryResult.filterNostrEvents(kind = NostrEventKind.LongFormContent),
            liveActivity = queryResult.filterNostrEvents(kind = NostrEventKind.LiveActivity),
            cdnResources = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalCdnResource),
            blossomServers = queryResult.filterNostrEvents(kind = NostrEventKind.BlossomServerList),
            eventStats = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalEventStats),
            wordCount = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalLongFormWordsCount),
            relayHints = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalRelayHint),
            primalUserNames = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalPremiumInfo),
        )

    override suspend fun getZapReceipts(invoices: List<String>) =
        runCatching {
            val queryResult = primalApiClient.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.INVOICES_TO_ZAP_RECEIPTS.id,
                    optionsJson = InvoicesToZapReceiptsRequest(invoices = invoices).encodeToJsonString(),
                ),
            )

            InvoicesToZapReceiptsResponse(
                mapEvent = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalInvoicesToZapRequests),
            )
        }

    override suspend fun getNip46Events(eventIds: List<String>): Result<EventsNip46Response> =
        runCatching {
            val queryResult = primalApiClient.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.EVENTS_NIP46.id,
                    optionsJson = EventsNip46Request(eventIds = eventIds).encodeToJsonString(),
                ),
            )

            EventsNip46Response(
                nip46Events = queryResult.filterNostrEvents(kind = NostrEventKind.NostrConnect),
            )
        }
}
