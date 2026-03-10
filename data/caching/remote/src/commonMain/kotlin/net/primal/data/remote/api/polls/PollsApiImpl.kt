package net.primal.data.remote.api.polls

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.polls.model.PollVotesRequestBody
import net.primal.data.remote.api.polls.model.PollVotesResponse
import net.primal.domain.nostr.NostrEventKind

internal class PollsApiImpl(
    private val primalApiClient: PrimalApiClient,
) : PollsApi {

    override suspend fun getPollVotes(body: PollVotesRequestBody): PollVotesResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.POLL_VOTES.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return PollVotesResponse(
            profiles = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            pollResponses = queryResult.filterNostrEvents(NostrEventKind.PollResponse),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            pollStats = queryResult.findPrimalEvent(NostrEventKind.PrimalPollStats),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
            referencedEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
        )
    }
}
