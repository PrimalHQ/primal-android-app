package net.primal.android.feed.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.feed.api.model.FeedRequestBody
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.api.model.NotesRequestBody
import net.primal.android.feed.api.model.ThreadRequestBody
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb.FEED_DIRECTIVE
import net.primal.android.networking.primal.PrimalVerb.NOTES
import net.primal.android.networking.primal.PrimalVerb.THREAD_VIEW
import net.primal.android.nostr.model.NostrEventKind

class FeedApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : FeedApi {

    override suspend fun getFeed(body: FeedRequestBody): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = FEED_DIRECTIVE,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            reposts = queryResult.filterNostrEvents(NostrEventKind.Reposts),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalEventUserStats,
            ),
            cdnResources = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalCdnResource,
            ),
            primalLinkPreviews = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalLinkPreview,
            ),
            referencedPosts = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
        )
    }

    override suspend fun getThread(body: ThreadRequestBody): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = THREAD_VIEW,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return FeedResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            reposts = emptyList(),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalEventUserStats,
            ),
            cdnResources = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalCdnResource,
            ),
            primalLinkPreviews = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalLinkPreview,
            ),
            referencedPosts = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
        )
    }

    override suspend fun getNotes(noteIds: Set<String>, extendedResponse: Boolean): FeedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = NOTES,
                optionsJson = NostrJson.encodeToString(
                    NotesRequestBody(noteIds = noteIds.toList(), extendedResponse = true),
                ),
            ),
        )

        return FeedResponse(
            paging = null,
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            posts = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            reposts = emptyList(),
            referencedPosts = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalEventUserStats,
            ),
            primalLinkPreviews = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalLinkPreview,
            ),
            cdnResources = queryResult.filterPrimalEvents(
                NostrEventKind.PrimalCdnResource,
            ),
        )
    }
}
