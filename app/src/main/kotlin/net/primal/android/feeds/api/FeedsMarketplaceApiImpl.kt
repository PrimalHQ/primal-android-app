package net.primal.android.feeds.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb.GET_FEATURED_DVM_READS
import net.primal.android.nostr.model.NostrEventKind

class FeedsMarketplaceApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : FeedsMarketplaceApi {
    override suspend fun getFeaturedReadsFeeds(): DvmFeedsResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = GET_FEATURED_DVM_READS,
                optionsJson = NostrJson.encodeToString(DvmFeedsRequestBody(kind = "reads")),
            ),
        )

        return DvmFeedsResponse(
            scores = queryResult.filterPrimalEvents(kind = NostrEventKind.PrimalEventStats),
            dvmHandlers = queryResult.filterNostrEvents(kind = NostrEventKind.AppHandler),
        )
    }
}
