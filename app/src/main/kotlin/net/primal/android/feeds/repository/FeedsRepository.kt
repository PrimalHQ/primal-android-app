package net.primal.android.feeds.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.articles.db.ArticleFeed
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.db.PrimalDatabase
import net.primal.android.feeds.api.FeedsApi
import net.primal.android.feeds.api.model.FeedsResponse
import net.primal.android.nostr.ext.findFirstIdentifier
import net.primal.android.nostr.model.primal.content.ContentAppSubSettings
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData
import net.primal.android.nostr.model.primal.content.ContentPrimalDvmFeedMetadata
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.android.user.accounts.active.ActiveAccountStore

class FeedsRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedsApi: FeedsApi,
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
) {

    fun observeReadsFeeds() = database.articleFeeds().observeAllFeeds()

    suspend fun fetchAndPersistArticleFeeds() {
        withContext(dispatcherProvider.io()) {
            val feeds = parseFeeds { feedsApi.getReadsUserFeeds(userId = activeAccountStore.activeUserId()) }
            if (feeds != null) {
                database.withTransaction {
                    database.articleFeeds().deleteAll()
                    database.articleFeeds().upsertAll(data = feeds)
                }
            }
        }
    }

    private suspend fun parseFeeds(feedsApiCall: suspend () -> FeedsResponse): List<ArticleFeed>? {
        return withContext(dispatcherProvider.io()) {
            val response = NostrJson.decodeFromStringOrNull<ContentAppSubSettings<List<ContentArticleFeedData>>>(
                string = feedsApiCall().articleFeeds.content,
            )

            response?.settings?.map { it.asArticleFeedPO() }
        }
    }

    private fun ContentArticleFeedData.asArticleFeedPO(): ArticleFeed {
        return ArticleFeed(
            spec = this.spec,
            name = this.name,
            description = this.description,
            enabled = this.enabled,
            kind = this.feedKind,
        )
    }

    suspend fun fetchDefaultArticleFeeds() =
        withContext(dispatcherProvider.io()) {
            parseFeeds { feedsApi.getDefaultReadsUserFeeds() }
        }

    suspend fun persistArticleFeeds(feeds: List<ArticleFeed>) {
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                database.articleFeeds().deleteAll()
                database.articleFeeds().upsertAll(data = feeds)
            }

            feedsApi.setReadsUserFeeds(
                userId = activeAccountStore.activeUserId(),
                feeds = feeds.map {
                    ContentArticleFeedData(
                        name = it.name,
                        spec = it.spec,
                        feedKind = it.kind,
                        description = it.description,
                        enabled = it.enabled,
                    )
                },
            )
        }
    }

    suspend fun fetchRecommendedDvmFeeds(): List<DvmFeed> {
        val response = withContext(dispatcherProvider.io()) { feedsApi.getFeaturedReadsFeeds() }
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

    suspend fun addDvmFeed(dvmFeed: DvmFeed) {
        withContext(dispatcherProvider.io()) {
            val feed = ArticleFeed(
                spec = dvmFeed.dvmSpec,
                name = dvmFeed.title,
                description = dvmFeed.description,
            )
            database.articleFeeds().upsertAll(listOf(feed))
        }
    }

    suspend fun removeFeed(feedSpec: String) {
        withContext(dispatcherProvider.io()) {
            database.articleFeeds().delete(feedSpec)
        }
    }

    suspend fun clearDvmFeed(dvmFeed: DvmFeed) {
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                database.articleFeedsConnections().deleteConnectionsBySpec(dvmFeed.dvmSpec)
            }
        }
    }
}
