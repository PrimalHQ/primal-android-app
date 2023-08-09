package net.primal.android.explore.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.explore.api.model.HashtagScore
import net.primal.android.explore.api.model.SearchUsersRequestBody
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull
import javax.inject.Inject

class ExploreApiImpl @Inject constructor(
    private val primalApiClient: PrimalApiClient,
) : ExploreApi {

    override suspend fun getTrendingHashtags(): List<HashtagScore> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = "trending_hashtags_7d")
        )

        val trendingHashtagEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalTrendingHashtags)
        val hashtags = NostrJson.decodeFromStringOrNull<JsonArray>(trendingHashtagEvent?.content)

        val result = mutableListOf<HashtagScore>()
        hashtags?.forEach {
            it.jsonObject.forEach { hashtag, score ->
                result.add(HashtagScore(name = hashtag, score = score.jsonPrimitive.float))
            }
        }

        return result
    }

    override suspend fun getRecommendedUsers(): UsersResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = "get_recommended_users")
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
        )
    }

    override suspend fun searchUsers(body: SearchUsersRequestBody): UsersResponse{
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = "user_search",
                optionsJson = NostrJson.encodeToString(body),
            )
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
        )
    }
}
