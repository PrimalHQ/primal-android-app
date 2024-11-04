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
import net.primal.android.nostr.ext.asEventStatsPO
import net.primal.android.nostr.ext.asEventUserStatsPO
import net.primal.android.nostr.ext.findFirstIdentifier
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsProfileDataPO
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
    fun observeAllFeeds() = database.feeds().observeAllFeeds()

    fun observeReadsFeeds() = database.feeds().observeAllFeeds(specKind = FeedSpecKind.Reads)

    fun observeNotesFeeds() = database.feeds().observeAllFeeds(specKind = FeedSpecKind.Notes)

    fun observeFeeds(specKind: FeedSpecKind) = database.feeds().observeAllFeeds(specKind = specKind)

    fun observeContainsFeedSpec(feedSpec: String) = database.feeds().observeContainsFeed(feedSpec)

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
            val feeds = content?.map { it.asFeedPO(specKind = specKind) }

            if (feeds != null) {
                database.withTransaction {
                    database.feeds().deleteAll(specKind = specKind)
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
            database.feeds().observeAllFeeds(specKind = specKind).first()
        }
        val defaultFeeds = givenDefaultFeeds.ifEmpty { fetchDefaultFeeds(specKind = specKind) ?: emptyList() }

        val localFeedSpecs = localFeeds.map { it.spec }.toSet()
        val newFeeds = defaultFeeds.filterNot { localFeedSpecs.contains(it.spec) }

        if (newFeeds.isNotEmpty()) {
            val disabledNewFeeds = newFeeds.map { it.copy(enabled = false) }
            val mergedFeeds = localFeeds + disabledNewFeeds
            persistLocallyAndRemotelyUserFeeds(userId = userId, feeds = mergedFeeds, specKind = specKind)
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

    suspend fun persistRemotelyAllLocalUserFeeds(userId: String) {
        persistRemotelyLocalUserFeedsBySpecKind(userId = userId, specKind = FeedSpecKind.Notes)
        persistRemotelyLocalUserFeedsBySpecKind(userId = userId, specKind = FeedSpecKind.Reads)
    }

    private suspend fun persistRemotelyLocalUserFeedsBySpecKind(userId: String, specKind: FeedSpecKind) {
        val feeds = withContext(dispatcherProvider.io()) {
            database.feeds().getAllFeedsBySpecKind(specKind = specKind)
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
                database.feeds().deleteAll(specKind = specKind)
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
        val feeds = givenDefaultFeeds.ifEmpty { fetchDefaultFeeds(specKind = specKind) ?: return@withContext }
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

        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profiles = response.userMetadata.mapAsProfileDataPO(
            cdnResources = cdnResources,
            primalUserNames = primalUserNames,
        ).distinctBy { it.ownerId }
        val profileScores = response.userScores.map { it.takeContentAsPrimalUserScoresOrNull() }
            .fold(emptyMap<String, Float>()) { acc, map -> acc + map }

        withContext(dispatcherProvider.io()) {
            database.profiles().upsertAll(data = profiles)
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
                        isPrimalFeed = metadata[nostrEvent.id]?.isPrimal,
                        primalSpec = dvmMetadata.primalSpec,
                        actionUserIds = actionUserIds,
                    )
                } else {
                    null
                }
            }

        return dvmFeeds
    }

    suspend fun addDvmFeedLocally(dvmFeed: DvmFeed, specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            val feed = Feed(
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
        feedSpec: String,
        title: String,
        description: String,
        feedSpecKind: FeedSpecKind,
        feedKind: String,
    ) {
        withContext(dispatcherProvider.io()) {
            val feed = Feed(
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

    suspend fun removeFeedLocally(feedSpec: String) {
        withContext(dispatcherProvider.io()) {
            database.feeds().delete(feedSpec)
        }
    }
}

inline fun <reified T> List<PrimalEvent>.parseAndMapContentByKey(key: T.() -> String): Map<String, T> =
    this.mapNotNull { primalEvent ->
        NostrJson.decodeFromStringOrNull<T>(primalEvent.content)
    }.asMapByKey { it.key() }
