package net.primal.android.feeds.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.db.PrimalDatabase
import net.primal.android.feeds.api.FeedsApi
import net.primal.android.feeds.db.Feed
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.FEED_KIND_DVM
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.nostr.ext.findFirstIdentifier
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

    fun observeReadsFeeds() = database.feeds().observeAllFeeds(specKind = FeedSpecKind.Reads)

    fun observeNotesFeeds() = database.feeds().observeAllFeeds(specKind = FeedSpecKind.Notes)

    fun observeFeeds(specKind: FeedSpecKind) = database.feeds().observeAllFeeds(specKind = specKind)

    suspend fun fetchAndPersistArticleFeeds() = fetchAndPersistFeeds(specKind = FeedSpecKind.Reads)

    suspend fun fetchAndPersistNoteFeeds() = fetchAndPersistFeeds(specKind = FeedSpecKind.Notes)

    private suspend fun fetchAndPersistFeeds(specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            val response = feedsApi.getUserFeeds(userId = activeAccountStore.activeUserId(), specKind = specKind)
            val content = NostrJson.decodeFromStringOrNull<List<ContentArticleFeedData>>(
                string = response.articleFeeds.content,
            )
            val feeds = content?.map { it.asFeedPO(specKind = specKind) }

            if (feeds != null) {
                database.withTransaction {
                    database.feeds().deleteAll(specKind = specKind)
                    database.feeds().upsertAll(data = feeds)
                }
            }
        }
    }

    suspend fun persistNewDefaultFeeds(specKind: FeedSpecKind) {
        val localFeeds = withContext(dispatcherProvider.io()) {
            database.feeds().observeAllFeeds(specKind = specKind).first()
        }
        val defaultFeeds = fetchDefaultFeeds(specKind = specKind) ?: emptyList()

        val localFeedSpecs = localFeeds.map { it.spec }.toSet()
        val newFeeds = defaultFeeds.toMutableList().apply {
            removeIf { localFeedSpecs.contains(it.spec) }
        }

        if (newFeeds.isNotEmpty()) {
            val disabledNewFeeds = newFeeds.map { it.copy(enabled = false) }
            val mergedFeeds = localFeeds + disabledNewFeeds
            persistArticleFeeds(feeds = mergedFeeds, specKind = specKind)
        }
    }

    private suspend fun fetchDefaultFeeds(specKind: FeedSpecKind): List<Feed>? {
        return withContext(dispatcherProvider.io()) {
            val response = feedsApi.getDefaultUserFeeds(specKind = specKind)
            val content = NostrJson.decodeFromStringOrNull<List<ContentArticleFeedData>>(
                string = response.articleFeeds.content,
            )

            content?.map { it.asFeedPO(specKind = specKind) }
        }
    }

    suspend fun persistArticleFeeds(feeds: List<Feed>, specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                database.feeds().deleteAll(specKind = specKind)
                database.feeds().upsertAll(data = feeds)
            }

            val apiFeeds = feeds.map {
                ContentArticleFeedData(
                    name = it.name,
                    spec = it.spec,
                    feedKind = it.feedKind,
                    description = it.description,
                    enabled = it.enabled,
                )
            }

            feedsApi.setUserFeeds(
                userId = activeAccountStore.activeUserId(),
                specKind = specKind,
                feeds = apiFeeds,
            )
        }
    }

    suspend fun fetchAndPersistDefaultFeeds(specKind: FeedSpecKind) =
        withContext(dispatcherProvider.io()) {
            val feeds = fetchDefaultFeeds(specKind = specKind) ?: return@withContext
            persistArticleFeeds(feeds = feeds, specKind = specKind)
        }

    suspend fun fetchRecommendedDvmFeeds(specKind: FeedSpecKind): List<DvmFeed> {
        val response = withContext(dispatcherProvider.io()) { feedsApi.getFeaturedFeeds(specKind) }
        val eventStatsMap = response.scores.mapNotNull { primalEvent ->
            NostrJson.decodeFromStringOrNull<ContentPrimalEventStats>(primalEvent.content)
        }.asMapByKey { it.eventId }

        val dvmFeeds = response.dvmHandlers
            .filter { it.content.isNotEmpty() }
            .mapNotNull { nostrEvent ->
                val dvmMetadata = NostrJson.decodeFromStringOrNull<ContentPrimalDvmFeedMetadata>(nostrEvent.content)
                val dvmId = nostrEvent.tags.findFirstIdentifier()
                val dvmTitle = dvmMetadata?.name
                if (dvmMetadata != null && dvmId != null && dvmTitle != null) {
                    DvmFeed(
                        dvmId = dvmId,
                        dvmPubkey = nostrEvent.pubKey,
                        avatarUrl = dvmMetadata.picture,
                        title = dvmTitle,
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

    suspend fun addReadsDvmFeed(dvmFeed: DvmFeed, specKind: FeedSpecKind) =
        addDvmFeed(dvmFeed = dvmFeed, specKind = specKind)

    private suspend fun addDvmFeed(dvmFeed: DvmFeed, specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            val feed = Feed(
                spec = dvmFeed.buildSpec(specKind = specKind),
                specKind = specKind,
                name = dvmFeed.title,
                description = dvmFeed.description ?: "",
                feedKind = FEED_KIND_DVM,
            )
            database.feeds().upsertAll(listOf(feed))
        }
    }

    suspend fun removeFeed(feedSpec: String) {
        withContext(dispatcherProvider.io()) {
            database.feeds().delete(feedSpec)
        }
    }

    suspend fun clearReadsDvmFeed(dvmFeed: DvmFeed, specKind: FeedSpecKind) =
        clearDvmFeed(dvmFeed = dvmFeed, specKind = specKind)

    private suspend fun clearDvmFeed(dvmFeed: DvmFeed, specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                database.articleFeedsConnections().deleteConnectionsBySpec(dvmFeed.buildSpec(specKind = specKind))
            }
        }
    }
}
