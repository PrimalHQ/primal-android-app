package net.primal.android.explore.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.explore.api.model.HashtagScore
import net.primal.android.explore.api.model.SearchUsersRequestBody
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb.RECOMMENDED_USERS
import net.primal.android.networking.primal.PrimalVerb.TRENDING_HASHTAGS_7D
import net.primal.android.networking.primal.PrimalVerb.USER_SEARCH
import net.primal.android.nostr.model.NostrEventKind

class ExploreApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : ExploreApi {

    override suspend fun getTrendingHashtags(): List<HashtagScore> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = TRENDING_HASHTAGS_7D),
        )

        val trendingHashtagEvent = queryResult.findPrimalEvent(
            NostrEventKind.PrimalTrendingHashtags,
        )
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
            message = PrimalCacheFilter(primalVerb = RECOMMENDED_USERS),
        )

        return UsersResponse(
            contactsMetadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            userScores = queryResult.findPrimalEvent(NostrEventKind.PrimalUserScores),
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
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
        )
    }
}
