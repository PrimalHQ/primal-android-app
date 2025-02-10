package net.primal.android.feeds.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
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
import net.primal.android.nostr.ext.asEventStatsPO
import net.primal.android.nostr.ext.asEventUserStatsPO
import net.primal.android.nostr.ext.findFirstIdentifier
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsMapPubkeyToListOfBlossomServers
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.ext.takeContentAsPrimalUserScoresOrNull
import net.primal.android.nostr.mappers.asContentArticleFeedData
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData
import net.primal.android.nostr.model.primal.content.ContentDvmFeedFollowsAction
import net.primal.android.nostr.model.primal.content.ContentDvmFeedMetadata
import net.primal.android.nostr.model.primal.content.ContentPrimalDvmFeedMetadata
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats
import net.primal.android.wallet.api.parseAsLNUrlOrNull

class FeedsRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedsApi: FeedsApi,
    private val database: PrimalDatabase,
) {
    fun observeAllFeeds(userId: String) = database.feeds().observeAllFeeds(ownerId = userId).distinctUntilChanged()

    fun observeReadsFeeds(userId: String) =
        database.feeds().observeAllFeedsBySpecKind(ownerId = userId, specKind = FeedSpecKind.Reads)
            .distinctUntilChanged()

    fun observeNotesFeeds(userId: String) =
        database.feeds().observeAllFeedsBySpecKind(ownerId = userId, specKind = FeedSpecKind.Notes)
            .distinctUntilChanged()

    fun observeFeeds(userId: String, specKind: FeedSpecKind) =
        database.feeds().observeAllFeedsBySpecKind(ownerId = userId, specKind = specKind)
            .distinctUntilChanged()

    fun observeContainsFeedSpec(userId: String, feedSpec: String) =
        database.feeds().observeContainsFeed(ownerId = userId, feedSpec)

    suspend fun fetchAndPersistArticleFeeds(userId: String) =
        fetchAndPersistFeeds(userId = userId, specKind = FeedSpecKind.Reads)

    suspend fun fetchAndPersistNoteFeeds(userId: String) =
        fetchAndPersistFeeds(userId = userId, specKind = FeedSpecKind.Notes)

    private suspend fun fetchAndPersistFeeds(userId: String, specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            val response = feedsApi.getUserFeeds(userId = userId, specKind = specKind)
            val content = NostrJson.decodeFromStringOrNull<List<ContentArticleFeedData>>(
                string = response.articleFeeds.content,
            )
            val feeds = content?.map { it.asFeedPO(ownerId = userId, specKind = specKind) }

            if (feeds != null) {
                database.withTransaction {
                    database.feeds().deleteAllByOwnerIdAndSpecKind(ownerId = userId, specKind = specKind)
                    database.feeds().upsertAll(data = feeds)
                }
            }
        }
    }

    suspend fun persistNewDefaultFeeds(
        userId: String,
        givenDefaultFeeds: List<Feed>,
        specKind: FeedSpecKind,
    ) {
        val localFeeds = withContext(dispatcherProvider.io()) {
            database.feeds().getAllFeedsBySpecKind(ownerId = userId, specKind = specKind)
        }
        val defaultFeeds = givenDefaultFeeds
            .ifEmpty { fetchDefaultFeeds(userId = userId, specKind = specKind) ?: emptyList() }

        val localFeedSpecs = localFeeds.map { it.spec }.toSet()
        val newFeeds = defaultFeeds.filterNot { localFeedSpecs.contains(it.spec) }

        if (newFeeds.isNotEmpty()) {
            val disabledNewFeeds = newFeeds.map { it.copy(enabled = false) }
            val mergedFeeds = localFeeds + disabledNewFeeds
            persistLocallyAndRemotelyUserFeeds(userId = userId, feeds = mergedFeeds, specKind = specKind)
        }
    }

    suspend fun fetchDefaultFeeds(userId: String, specKind: FeedSpecKind): List<Feed>? {
        return withContext(dispatcherProvider.io()) {
            val response = feedsApi.getDefaultUserFeeds(specKind = specKind)
            val content = NostrJson.decodeFromStringOrNull<List<ContentArticleFeedData>>(
                string = response.articleFeeds.content,
            )

            content?.map { it.asFeedPO(ownerId = userId, specKind = specKind) }
        }
    }

    suspend fun persistRemotelyAllLocalUserFeeds(userId: String) {
        persistRemotelyLocalUserFeedsBySpecKind(userId = userId, specKind = FeedSpecKind.Notes)
        persistRemotelyLocalUserFeedsBySpecKind(userId = userId, specKind = FeedSpecKind.Reads)
    }

    private suspend fun persistRemotelyLocalUserFeedsBySpecKind(userId: String, specKind: FeedSpecKind) {
        val feeds = withContext(dispatcherProvider.io()) {
            database.feeds().getAllFeedsBySpecKind(ownerId = userId, specKind = specKind)
        }

        feedsApi.setUserFeeds(
            userId = userId,
            specKind = specKind,
            feeds = feeds.map { it.asContentArticleFeedData() },
        )
    }

    suspend fun persistLocallyAndRemotelyUserFeeds(
        userId: String,
        feeds: List<Feed>,
        specKind: FeedSpecKind,
    ) {
        withContext(dispatcherProvider.io()) {
            database.withTransaction {
                database.feeds().deleteAllByOwnerIdAndSpecKind(ownerId = userId, specKind = specKind)
                database.feeds().upsertAll(data = feeds)
            }

            val apiFeeds = feeds.map { it.asContentArticleFeedData() }

            feedsApi.setUserFeeds(
                userId = userId,
                specKind = specKind,
                feeds = apiFeeds,
            )
        }
    }

    suspend fun fetchAndPersistDefaultFeeds(
        userId: String,
        givenDefaultFeeds: List<Feed>,
        specKind: FeedSpecKind,
    ) = withContext(dispatcherProvider.io()) {
        val feeds = givenDefaultFeeds
            .ifEmpty { fetchDefaultFeeds(userId = userId, specKind = specKind) ?: return@withContext }
        persistLocallyAndRemotelyUserFeeds(userId = userId, feeds = feeds, specKind = specKind)
    }

    suspend fun fetchRecommendedDvmFeeds(userId: String, specKind: FeedSpecKind? = null): List<DvmFeed> {
        val response = withContext(dispatcherProvider.io()) {
            feedsApi.getFeaturedFeeds(specKind = specKind, pubkey = userId)
        }
        val eventStatsMap = response.scores.parseAndMapContentByKey<ContentPrimalEventStats> { eventId }
        val metadata = response.feedMetadata.parseAndMapContentByKey<ContentDvmFeedMetadata> { eventId }
        val userStats = response.feedUserStats.parseAndMapContentByKey<ContentPrimalEventUserStats> { eventId }
        val followsActions = response.feedFollowActions.parseAndMapContentByKey<ContentDvmFeedFollowsAction> { eventId }

        val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
        val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
        val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
        val profiles = response.userMetadata.mapAsProfileDataPO(
            cdnResources = cdnResources,
            primalUserNames = primalUserNames,
            primalPremiumInfo = primalPremiumInfo,
            primalLegendProfiles = primalLegendProfiles,
            blossomServers = blossomServers,
        ).distinctBy { it.ownerId }
        val profileScores = response.userScores.map { it.takeContentAsPrimalUserScoresOrNull() }
            .fold(emptyMap<String, Float>()) { acc, map -> acc + map }

        withContext(dispatcherProvider.io()) {
            database.profiles().insertOrUpdateAll(data = profiles)
            database.eventStats().upsertAll(data = eventStatsMap.values.map { it.asEventStatsPO() })
            database.eventUserStats().upsertAll(data = userStats.values.map { it.asEventUserStatsPO(userId = userId) })
        }

        val dvmFeeds = response.dvmHandlers
            .filter { it.content.isNotEmpty() }
            .mapNotNull { nostrEvent ->
                val dvmMetadata = NostrJson.decodeFromStringOrNull<ContentPrimalDvmFeedMetadata>(nostrEvent.content)
                val dvmId = nostrEvent.tags.findFirstIdentifier()
                val dvmTitle = dvmMetadata?.name

                val actionUserIds = followsActions[nostrEvent.id]?.userIds
                    ?.sortedBy { profileScores[it] }
                    ?: emptyList()

                if (dvmMetadata != null && dvmId != null && dvmTitle != null) {
                    DvmFeed(
                        eventId = nostrEvent.id,
                        dvmId = dvmId,
                        dvmPubkey = nostrEvent.pubKey,
                        dvmLnUrlDecoded = dvmMetadata.lud16?.parseAsLNUrlOrNull(),
                        avatarUrl = dvmMetadata.picture ?: dvmMetadata.image,
                        title = dvmTitle,
                        description = dvmMetadata.about,
                        amountInSats = dvmMetadata.amount,
                        primalSubscriptionRequired = dvmMetadata.subscription == true,
                        kind = when (metadata[nostrEvent.id]?.kind?.lowercase()) {
                            "notes" -> FeedSpecKind.Notes
                            "reads" -> FeedSpecKind.Reads
                            else -> null
                        },
                        primalSpec = dvmMetadata.primalSpec,
                        isPrimalFeed = dvmMetadata.primalSpec?.isNotEmpty() == true,
                        actionUserIds = actionUserIds,
                    )
                } else {
                    null
                }
            }

        return dvmFeeds
    }

    suspend fun addDvmFeedLocally(userId: String, dvmFeed: DvmFeed, specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            val feed = Feed(
                ownerId = userId,
                spec = dvmFeed.buildSpec(specKind = specKind),
                specKind = specKind,
                title = dvmFeed.title,
                description = dvmFeed.description ?: "",
                feedKind = FEED_KIND_DVM,
            )
            database.feeds().upsertAll(listOf(feed))
        }
    }

    suspend fun addFeedLocally(
        userId: String,
        feedSpec: String,
        title: String,
        description: String,
        feedSpecKind: FeedSpecKind,
        feedKind: String,
    ) {
        withContext(dispatcherProvider.io()) {
            val feed = Feed(
                ownerId = userId,
                spec = feedSpec,
                specKind = feedSpecKind,
                feedKind = feedKind,
                enabled = true,
                title = title,
                description = description,
            )
            database.feeds().upsertAll(listOf(feed))
        }
    }

    suspend fun removeFeedLocally(userId: String, feedSpec: String) {
        withContext(dispatcherProvider.io()) {
            database.feeds().deleteAllByOwnerIdAndSpec(ownerId = userId, spec = feedSpec)
        }
    }
}

inline fun <reified T> List<PrimalEvent>.parseAndMapContentByKey(key: T.() -> String): Map<String, T> =
    this.mapNotNull { primalEvent ->
        NostrJson.decodeFromStringOrNull<T>(primalEvent.content)
    }.asMapByKey { it.key() }
