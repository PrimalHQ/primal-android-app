package net.primal.android.explore.api

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.explore.api.model.HashtagScore
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.networking.sockets.model.OutgoingMessage
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import javax.inject.Inject

class ExploreApiImpl @Inject constructor(
    private val socketClient: SocketClient,
) : ExploreApi {

    override suspend fun getTrendingHashtags(): List<HashtagScore> {
        val queryResult = socketClient.query(
            message = OutgoingMessage(primalVerb = "trending_hashtags_7d")
        )

        val trendingHashtagEvent = queryResult.primalEvents.find {
            NostrEventKind.PrimalTrendingHashtags.value == it.kind
        } ?: return emptyList()

        val hashtags = NostrJson.decodeFromString<JsonArray>(trendingHashtagEvent.content)


        val result = mutableListOf<HashtagScore>()
        hashtags.forEach {
            it.jsonObject.forEach { hashtag, score ->
                result.add(HashtagScore(name = hashtag, score = score.jsonPrimitive.float))
            }
        }

        return result
    }
}
