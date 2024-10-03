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
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.takeContentAsPrimalUserScoresOrNull
import net.primal.android.nostr.mappers.asContentArticleFeedData
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData
import net.primal.android.nostr.model.primal.content.ContentDvmFeedFollowsAction
import net.primal.android.nostr.model.primal.content.ContentDvmFeedMetadata
import net.primal.android.nostr.model.primal.content.ContentPrimalDvmFeedMetadata
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats
import net.primal.android.user.accounts.active.ActiveAccountStore

class FeedsRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedsApi: FeedsApi,
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
) {
    fun observeAllFeeds() = database.feeds().observeAllFeeds()

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

    suspend fun persistNewDefaultFeeds(givenDefaultFeeds: List<Feed>, specKind: FeedSpecKind) {
        val localFeeds = withContext(dispatcherProvider.io()) {
            database.feeds().observeAllFeeds(specKind = specKind).first()
        }
        val defaultFeeds = givenDefaultFeeds.ifEmpty { fetchDefaultFeeds(specKind = specKind) ?: emptyList() }

        val localFeedSpecs = localFeeds.map { it.spec }.toSet()
        val newFeeds = defaultFeeds.filterNot { localFeedSpecs.contains(it.spec) }

        if (newFeeds.isNotEmpty()) {
            val disabledNewFeeds = newFeeds.map { it.copy(enabled = false) }
            val mergedFeeds = localFeeds + disabledNewFeeds
            persistGivenUserFeeds(feeds = mergedFeeds, specKind = specKind)
        }
    }

    suspend fun fetchDefaultFeeds(specKind: FeedSpecKind): List<Feed>? {
        return withContext(dispatcherProvider.io()) {
            val response = feedsApi.getDefaultUserFeeds(specKind = specKind)
            val content = NostrJson.decodeFromStringOrNull<List<ContentArticleFeedData>>(
                string = response.articleFeeds.content,
            )

            content?.map { it.asFeedPO(specKind = specKind) }
        }
    }

    suspend fun persistAllLocalUserFeeds(userId: String) {
        persistLocalUserFeedsBySpecKind(userId = userId, specKind = FeedSpecKind.Notes)
        persistLocalUserFeedsBySpecKind(userId = userId, specKind = FeedSpecKind.Reads)
    }

    private suspend fun persistLocalUserFeedsBySpecKind(userId: String, specKind: FeedSpecKind) {
        val feeds = withContext(dispatcherProvider.io()) {
            database.feeds().getAllFeedsBySpecKind(specKind = specKind)
        }

        feedsApi.setUserFeeds(
            userId = userId,
            specKind = FeedSpecKind.Notes,
            feeds = feeds.map { it.asContentArticleFeedData() },
        )
    }


    suspend fun persistGivenUserFeeds(feeds: List<Feed>, specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                database.feeds().deleteAll(specKind = specKind)
                database.feeds().upsertAll(data = feeds)
            }

            val apiFeeds = feeds.map { it.asContentArticleFeedData() }

            feedsApi.setUserFeeds(
                userId = activeAccountStore.activeUserId(),
                specKind = specKind,
                feeds = apiFeeds,
            )
        }
    }

    suspend fun fetchAndPersistDefaultFeeds(givenDefaultFeeds: List<Feed>, specKind: FeedSpecKind) =
        withContext(dispatcherProvider.io()) {
            val feeds = givenDefaultFeeds.ifEmpty { fetchDefaultFeeds(specKind = specKind) ?: return@withContext }
            persistGivenUserFeeds(feeds = feeds, specKind = specKind)
        }

    suspend fun fetchRecommendedDvmFeeds(specKind: FeedSpecKind? = null, pubkey: String? = null): List<DvmFeed> {
        val response = withContext(dispatcherProvider.io()) {
            feedsApi.getFeaturedFeeds(specKind = specKind, pubkey = pubkey)
        }
        val eventStatsMap = response.scores.parseAndMapContentByKey<ContentPrimalEventStats> { eventId }
        val metadatas = response.feedMetadatas.parseAndMapContentByKey<ContentDvmFeedMetadata> { eventId }
        val userStats = response.feedUserStats.parseAndMapContentByKey<ContentPrimalEventUserStats> { eventId }
        val followsActions = response.feedFollowActions.parseAndMapContentByKey<ContentDvmFeedFollowsAction> { eventId }

        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profiles = response.userMetadatas.mapAsProfileDataPO(cdnResources = cdnResources).distinctBy { it.ownerId }

        val profileScores = response.userScores.map { it.takeContentAsPrimalUserScoresOrNull() }
            .fold(emptyMap<String, Float>()) { acc, map -> acc + map }

        withContext(dispatcherProvider.io()) {
            database.profiles().upsertAll(data = profiles)
        }

        val dvmFeeds = response.dvmHandlers
            .filter { it.content.isNotEmpty() }
            .mapNotNull { nostrEvent ->
                val dvmMetadata = NostrJson.decodeFromStringOrNull<ContentPrimalDvmFeedMetadata>(nostrEvent.content)
                val dvmId = nostrEvent.tags.findFirstIdentifier()
                val dvmTitle = dvmMetadata?.name
                val profileDatasFromFollowsActions = profiles
                    .filter { followsActions[nostrEvent.id]?.userIds?.contains(it.ownerId) == true }
                    .sortedByDescending { profileData -> profileScores[profileData.ownerId] }

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
                        kind = when (metadatas[nostrEvent.id]?.kind?.lowercase()) {
                            "notes" -> FeedSpecKind.Notes
                            "reads" -> FeedSpecKind.Reads
                            else -> null
                        },
                        isPrimal = metadatas[nostrEvent.id]?.isPrimal,
                        followsActions = profileDatasFromFollowsActions,
                        userLiked = userStats[nostrEvent.id]?.liked,
                        userZapped = userStats[nostrEvent.id]?.zapped,
                    )
                } else {
                    null
                }
            }

        return dvmFeeds
    }

    suspend fun addReadsDvmFeed(dvmFeed: DvmFeed, specKind: FeedSpecKind) =
        addDvmFeed(dvmFeed = dvmFeed, specKind = specKind)

    suspend fun addDvmFeed(dvmFeed: DvmFeed, specKind: FeedSpecKind) {
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

    private inline fun <reified T> List<PrimalEvent>.parseAndMapContentByKey(key: T.() -> String): Map<String, T> =
        this.mapNotNull { primalEvent ->
            NostrJson.decodeFromStringOrNull<T>(primalEvent.content)
        }.asMapByKey { it.key() }
}
