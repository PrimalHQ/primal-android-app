package net.primal.android.feeds.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.db.PrimalDatabase
import net.primal.android.feeds.api.FeedsMarketplaceApi
import net.primal.android.nostr.ext.findFirstIdentifier
import net.primal.android.nostr.model.primal.content.ContentPrimalDvmFeedMetadata
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats

class FeedMarketplaceRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedsMarketplaceApi: FeedsMarketplaceApi,
    private val primalDatabase: PrimalDatabase,
) {

    suspend fun fetchRecommendedDvmFeeds(): List<DvmFeed> {
        val response = withContext(dispatcherProvider.io()) { feedsMarketplaceApi.getFeaturedReadsFeeds() }
        val eventStatsMap = response.scores.mapNotNull { primalEvent ->
            NostrJson.decodeFromStringOrNull<ContentPrimalEventStats>(primalEvent.content)
        }.asMapByKey { it.eventId }

        val dvmFeeds = response.dvmHandlers
            .filter { it.content.isNotEmpty() }
            .mapNotNull { nostrEvent ->
                val dvmMetadata = NostrJson.decodeFromStringOrNull<ContentPrimalDvmFeedMetadata>(nostrEvent.content)
                val dvmId = nostrEvent.tags.findFirstIdentifier()
                if (dvmMetadata != null && dvmId != null) {
                    DvmFeed(
                        dvmId = dvmId,
                        dvmPubkey = nostrEvent.pubKey,
                        avatarUrl = dvmMetadata.picture,
                        title = dvmMetadata.name,
                        description = dvmMetadata.about,
                        amountInSats = dvmMetadata.amount,
                        primalSubscriptionRequired = dvmMetadata.subscription == true,
                        totalLikes = eventStatsMap[nostrEvent.id]?.likes,
                        totalSatsZapped = eventStatsMap[nostrEvent.id]?.satsZapped,
                    )
                } else {
                    null
                }
            }

        return dvmFeeds
    }

    suspend fun clearDvmFeed(dvmFeed: DvmFeed) {
        withContext(dispatcherProvider.io()) {
            primalDatabase.withTransaction {
                primalDatabase.articleFeedsConnections().deleteConnectionsBySpec(dvmFeed.dvmSpec)
            }
        }
    }
}
