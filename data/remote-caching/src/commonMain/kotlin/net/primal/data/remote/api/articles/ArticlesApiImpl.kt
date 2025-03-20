package net.primal.data.remote.api.articles

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.CommonJsonImplicitNulls
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEventKind

internal class ArticlesApiImpl(
    private val primalApiClient: PrimalApiClient,
) : ArticlesApi {

    override suspend fun getArticleDetails(body: ArticleDetailsRequestBody): ArticleResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.ARTICLE_THREAD_VIEW.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return ArticleResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging)?.content.decodeFromJsonStringOrNull(),
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            notes = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            articles = queryResult.filterNostrEvents(NostrEventKind.LongFormContent),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalUserScores = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            referencedEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalLongFormWords = queryResult.filterPrimalEvents(NostrEventKind.PrimalLongFormWordsCount),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getArticleFeed(body: ArticleFeedRequestBody): ArticleResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MEGA_FEED_DIRECTIVE.id,
                optionsJson = CommonJsonImplicitNulls.encodeToString(body),
            ),
        )

        return ArticleResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging)?.content.decodeFromJsonStringOrNull(),
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            notes = emptyList(),
            articles = queryResult.filterNostrEvents(NostrEventKind.LongFormContent),
            referencedEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalUserScores = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalLongFormWords = queryResult.filterPrimalEvents(NostrEventKind.PrimalLongFormWordsCount),
            primalUserNames = queryResult.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = queryResult.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = queryResult.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getArticleHighlights(body: ArticleHighlightsRequestBody): ArticleHighlightsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.GET_HIGHLIGHTS.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return ArticleHighlightsResponse(
            highlights = queryResult.filterNostrEvents(kind = NostrEventKind.Highlight),
            legendProfiles = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalPremiumInfo),
            primalUserNames = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalUserNames),
            primalUserScores = queryResult.findPrimalEvent(kind = NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalCdnResource),
            profileMetadatas = queryResult.filterNostrEvents(kind = NostrEventKind.Metadata),
            eventStats = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalEventStats),
            relayHints = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalRelayHint),
            zaps = queryResult.filterNostrEvents(kind = NostrEventKind.Zap),
            primalLongFormContentWordsCount = queryResult.filterPrimalEvents(
                kind = NostrEventKind.PrimalLongFormWordsCount,
            ),
            referencedEvents = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalReferencedEvent),
            highlightComments = queryResult.filterNostrEvents(kind = NostrEventKind.ShortTextNote),
            blossomServers = queryResult.filterNostrEvents(kind = NostrEventKind.BlossomServerList),
        )
    }
}
