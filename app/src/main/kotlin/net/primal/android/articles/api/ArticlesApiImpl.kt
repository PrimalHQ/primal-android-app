package net.primal.android.articles.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.articles.api.model.ArticleDetailsRequestBody
import net.primal.android.articles.api.model.ArticleDetailsResponse
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.model.NostrEventKind

class ArticlesApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : ArticlesApi {

    override suspend fun getArticleDetails(body: ArticleDetailsRequestBody): ArticleDetailsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.BLOG_THREAD_VIEW,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return ArticleDetailsResponse(
            paging = queryResult.findPrimalEvent(NostrEventKind.PrimalPaging).let {
                NostrJson.decodeFromStringOrNull(it?.content)
            },
            metadata = queryResult.filterNostrEvents(NostrEventKind.Metadata),
            notes = queryResult.filterNostrEvents(NostrEventKind.ShortTextNote),
            longFormContents = queryResult.filterNostrEvents(NostrEventKind.LongFormContent),
            zaps = queryResult.filterNostrEvents(NostrEventKind.Zap),
            primalEventStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventStats),
            primalEventUserStats = queryResult.filterPrimalEvents(NostrEventKind.PrimalEventUserStats),
            primalUserScores = queryResult.filterPrimalEvents(NostrEventKind.PrimalUserScores),
            cdnResources = queryResult.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalLinkPreviews = queryResult.filterPrimalEvents(NostrEventKind.PrimalLinkPreview),
            referencedNotes = queryResult.filterPrimalEvents(NostrEventKind.PrimalReferencedEvent),
            primalRelayHints = queryResult.filterPrimalEvents(NostrEventKind.PrimalRelayHint),
            primalLongFormWords = queryResult.filterPrimalEvents(NostrEventKind.PrimalLongFormWordsCount),
        )
    }
}
