package net.primal.data.remote.api.explore

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonPrimitive
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.data.remote.api.explore.model.ExploreRequestBody
import net.primal.data.remote.api.explore.model.SearchUsersRequestBody
import net.primal.data.remote.api.explore.model.TopicScore
import net.primal.data.remote.api.explore.model.TrendingPeopleResponse
import net.primal.data.remote.api.explore.model.TrendingZapsResponse
import net.primal.data.remote.api.explore.model.UsersResponse
import net.primal.domain.nostr.NostrEventKind

internal class ExploreApiImpl(
    private val primalApiClient: PrimalApiClient,
) : ExploreApi {

    override suspend fun getTrendingPeople(body: ExploreRequestBody): TrendingPeopleResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.EXPLORE_PEOPLE.id,
                optionsJson = CommonJson.encodeToString(body),
            ),
        )

        return TrendingPeopleResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                CommonJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            usersFollowStats = queryResult.findPrimalEvent(NostrEventKind.PrimalExplorePeopleNewFollowStats),
            usersScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            usersFollowCount = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getTrendingZaps(body: ExploreRequestBody): TrendingZapsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.EXPLORE_ZAPS.id,
                optionsJson = CommonJson.encodeToString(body),
            ),
        )

        return TrendingZapsResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                CommonJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            usersScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            nostrZapEvents = queryResult.filterNostrEvents(NostrEventKind.Zap),
            noteEvents = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getTrendingTopics(): List<TopicScore> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = net.primal.data.remote.PrimalVerb.EXPLORE_TOPICS.id),
        )

        val trendingTopicsEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalTrendingTopics)
        val topics = CommonJson.decodeFromStringOrNull<JsonObject>(trendingTopicsEvent?.content)

        val result = mutableListOf<TopicScore>()
        topics?.forEach { (topic, score) ->
            result.add(TopicScore(name = topic, score = score.jsonPrimitive.float))
        }

        return result
    }

    override suspend fun getPopularUsers(): UsersResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = net.primal.data.remote.PrimalVerb.RECOMMENDED_USERS.id),
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            followerCounts = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun searchUsers(body: SearchUsersRequestBody): UsersResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.USER_SEARCH.id,
                optionsJson = CommonJson.encodeToString(body),
            ),
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            followerCounts = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }
}
