package net.primal.android.explore.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.explore.api.model.ExploreRequestBody
import net.primal.android.explore.api.model.SearchUsersRequestBody
import net.primal.android.explore.api.model.TopicScore
import net.primal.android.explore.api.model.TrendingPeopleResponse
import net.primal.android.explore.api.model.TrendingZapsResponse
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb.EXPLORE_PEOPLE
import net.primal.android.networking.primal.PrimalVerb.EXPLORE_TOPICS
import net.primal.android.networking.primal.PrimalVerb.EXPLORE_ZAPS
import net.primal.android.networking.primal.PrimalVerb.RECOMMENDED_USERS
import net.primal.android.networking.primal.PrimalVerb.USER_SEARCH
import net.primal.android.nostr.model.NostrEventKind

class ExploreApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : ExploreApi {

    override suspend fun getTrendingPeople(body: ExploreRequestBody): TrendingPeopleResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = EXPLORE_PEOPLE,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return TrendingPeopleResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            usersFollowStats = queryResult.findPrimalEvent(NostrEventKind.PrimalExplorePeopleNewFollowStats),
            usersScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            usersFollowCount = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            primalUserNames = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserName),
        )
    }

    override suspend fun getTrendingZaps(body: ExploreRequestBody): TrendingZapsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = EXPLORE_ZAPS,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return TrendingZapsResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            usersScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            nostrZapEvents = queryResult.filterNostrEvents(NostrEventKind.Zap),
            noteEvents = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            primalUserNames = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserName),
        )
    }

    override suspend fun getTrendingTopics(): List<TopicScore> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = EXPLORE_TOPICS),
        )

        val trendingTopicsEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalTrendingTopics)
        val topics = NostrJson.decodeFromStringOrNull<JsonObject>(trendingTopicsEvent?.content)

        val result = mutableListOf<TopicScore>()
        topics?.forEach { topic, score ->
            result.add(TopicScore(name = topic, score = score.jsonPrimitive.float))
        }

        return result
    }

    override suspend fun getPopularUsers(): UsersResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = RECOMMENDED_USERS),
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            followerCounts = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserName),
        )
    }

    override suspend fun searchUsers(body: SearchUsersRequestBody): UsersResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = USER_SEARCH,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
            followerCounts = queryResult.findPrimalEvent(NostrEventKind.PrimalUserFollowersCounts),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserName),
        )
    }
}
