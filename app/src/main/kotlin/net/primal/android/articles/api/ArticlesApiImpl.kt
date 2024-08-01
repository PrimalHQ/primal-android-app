package net.primal.android.articles.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.articles.api.model.ArticleDetailsRequestBody
import net.primal.android.articles.api.model.ArticleFeedRequestBody
import net.primal.android.articles.api.model.ArticleFeedsResponse
import net.primal.android.articles.api.model.ArticleResponse
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEventKind
import timber.log.Timber

class ArticlesApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : ArticlesApi {

    override suspend fun getArticleDetails(body: ArticleDetailsRequestBody): ArticleResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.BLOG_THREAD_VIEW,
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
            primalArticles = emptyList(),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalUserScores = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            referencedEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalLongFormWords = queryResult.filterPrimalEvents(NostrEventKind.PrimalLongFormWordsCount),
        )
    }

    override suspend fun getArticleFeed(body: ArticleFeedRequestBody): ArticleResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.READS_FEED_DIRECTIVE,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return ArticleResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            notes = emptyList(),
            articles = emptyList(),
            primalArticles = queryResult.filterPrimalEvents(NostrEventKind.PrimalLongFormContent),
            referencedEvents = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalUserScores = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalLongFormWords = queryResult.filterPrimalEvents(NostrEventKind.PrimalLongFormWordsCount),
        )
    }

    override suspend fun getArticleFeeds(): ArticleFeedsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(primalVerb = PrimalVerb.READS_FEEDS),
        )

        val articleFeeds = queryResult.findPrimalEvent(NostrEventKind.PrimalLongFormContentFeeds)

        if (articleFeeds == null) {
            Timber.w(queryResult.toString())
            throw WssException("Invalid response.")
        }

        return ArticleFeedsResponse(articleFeeds = articleFeeds)
    }
}
