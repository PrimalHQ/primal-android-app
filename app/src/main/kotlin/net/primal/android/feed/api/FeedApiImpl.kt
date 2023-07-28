package net.primal.android.feed.api

import kotlinx.serialization.encodeToString
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.api.model.ThreadRequestBody
import net.primal.android.feed.api.model.ThreadResponse
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull
import javax.inject.Inject

class FeedApiImpl @Inject constructor(
    private val primalApiClient: PrimalApiClient,
) : FeedApi {

    override suspend fun getFeed(body: FeedRequestBody): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = "feed_directive",
                optionsJson = NostrJson.encodeToString(body)
            )
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            reposts = queryResult.filterNostrEvents(NostrEventKind.Reposts),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalEventResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventResources),
            referencedPosts = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
        )

    }

    override suspend fun getThread(body: ThreadRequestBody): ThreadResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = "thread_view",
                optionsJson = NostrJson.encodeToString(body)
            )
        )

        return ThreadResponse(
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalEventResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventResources),
            referencedPosts = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
        )
    }

}
