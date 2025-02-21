package net.primal.android.articles.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.articles.api.model.ArticleDetailsRequestBody
import net.primal.android.articles.api.model.ArticleFeedRequestBody
import net.primal.android.articles.api.model.ArticleHighlightsRequestBody
import net.primal.android.articles.api.model.ArticleHighlightsResponse
import net.primal.android.articles.api.model.ArticleResponse
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.NostrJsonImplicitNulls
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.model.NostrEventKind

class ArticlesApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : ArticlesApi {

    override suspend fun getArticleDetails(body: ArticleDetailsRequestBody): ArticleResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.ARTICLE_THREAD_VIEW,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return ArticleResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
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
                primalVerb = PrimalVerb.MEGA_FEED_DIRECTIVE,
                optionsJson = NostrJsonImplicitNulls.encodeToString(body),
            ),
        )

        return ArticleResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
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
                primalVerb = PrimalVerb.GET_HIGHLIGHTS,
                optionsJson = NostrJson.encodeToString(body),
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
