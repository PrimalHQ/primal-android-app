package net.primal.data.repository.feeds

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.AppBuildHelper
import net.primal.core.utils.asMapByKey
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.local.dao.feeds.Feed
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.feeds.FeedsApi
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.remote.model.ContentAppSubSettings
import net.primal.data.remote.model.ContentDvmFeedFollowsAction
import net.primal.data.remote.model.ContentDvmFeedMetadata
import net.primal.data.remote.model.ContentPrimalDvmFeedMetadata
import net.primal.data.remote.model.ContentPrimalEventStats
import net.primal.data.remote.model.ContentPrimalEventUserStats
import net.primal.data.remote.model.ContentPrimalFeedData
import net.primal.data.repository.mappers.local.asContentPrimalFeedData
import net.primal.data.repository.mappers.local.asFeedPO
import net.primal.data.repository.mappers.local.asPrimalFeedDO
import net.primal.data.repository.mappers.remote.asEventStatsPO
import net.primal.data.repository.mappers.remote.asEventUserStatsPO
import net.primal.data.repository.mappers.remote.asFeedPO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserScoresOrNull
import net.primal.domain.DvmFeed
import net.primal.domain.FEED_KIND_DVM
import net.primal.domain.FeedSpecKind
import net.primal.domain.PrimalEvent
import net.primal.domain.buildSpec
import net.primal.domain.model.PrimalFeed
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.findFirstIdentifier
import net.primal.domain.nostr.utils.parseAsLNUrlOrNull
import net.primal.domain.repository.FeedsRepository

// TODO Consider splitting the repository into smaller ones
class FeedsRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val feedsApi: FeedsApi,
    private val database: PrimalDatabase,
    private val signatureHandler: NostrEventSignatureHandler,
) : FeedsRepository {

    override fun observeAllFeeds(userId: String) =
        database.feeds().observeAllFeeds(ownerId = userId)
            .distinctUntilChanged()
            .map { it.map { it.asPrimalFeedDO() } }

    override fun observeReadsFeeds(userId: String) =
        database.feeds().observeAllFeedsBySpecKind(ownerId = userId, specKind = FeedSpecKind.Reads)
            .distinctUntilChanged()
            .map { it.map { it.asPrimalFeedDO() } }

    override fun observeNotesFeeds(userId: String) =
        database.feeds().observeAllFeedsBySpecKind(ownerId = userId, specKind = FeedSpecKind.Notes)
            .distinctUntilChanged()
            .map { it.map { it.asPrimalFeedDO() } }

    override fun observeFeeds(userId: String, specKind: FeedSpecKind) =
        database.feeds().observeAllFeedsBySpecKind(ownerId = userId, specKind = specKind)
            .distinctUntilChanged()
            .map { it.map { it.asPrimalFeedDO() } }

    override fun observeContainsFeedSpec(userId: String, feedSpec: String) =
        database.feeds().observeContainsFeed(ownerId = userId, feedSpec)

    override suspend fun fetchAndPersistArticleFeeds(userId: String) =
        fetchAndPersistFeeds(userId = userId, specKind = FeedSpecKind.Reads)

    override suspend fun fetchAndPersistNoteFeeds(userId: String) =
        fetchAndPersistFeeds(userId = userId, specKind = FeedSpecKind.Notes)

    private suspend fun fetchAndPersistFeeds(userId: String, specKind: FeedSpecKind) {
        withContext(dispatcherProvider.io()) {
            val authorization = signatureHandler.signNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.ApplicationSpecificData.value,
                    tags = listOf("${AppBuildHelper.getAppName()} App".asIdentifierTag()),
                    content = ContentAppSubSettings<String>(key = specKind.settingsKey).encodeToJsonString(),
                ),
            )
            val response = feedsApi.getUserFeeds(authorization = authorization, specKind = specKind)
            val content = CommonJson.decodeFromStringOrNull<List<ContentPrimalFeedData>>(
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

    override suspend fun persistNewDefaultFeeds(
        userId: String,
        specKind: FeedSpecKind,
        givenDefaultFeeds: List<PrimalFeed>,
    ) {
        val localFeeds = withContext(dispatcherProvider.io()) {
            database.feeds().getAllFeedsBySpecKind(ownerId = userId, specKind = specKind)
                .map { it.asPrimalFeedDO() }
        }
        val defaultFeeds = givenDefaultFeeds
            .ifEmpty { fetchDefaultFeeds(userId = userId, specKind = specKind) ?: emptyList() }

        val localFeedSpecs = localFeeds.map { it.spec }.toSet()
        val newFeeds = defaultFeeds.filterNot { localFeedSpecs.contains(it.spec) }

        if (newFeeds.isNotEmpty()) {
            val disabledNewFeeds = newFeeds.map { it.copy(enabled = false) }
            val mergedFeeds = localFeeds + disabledNewFeeds
            persistLocallyAndRemotelyUserFeeds(
                userId = userId,
                specKind = specKind,
                feeds = mergedFeeds,
            )
        }
    }

    override suspend fun fetchDefaultFeeds(userId: String, specKind: FeedSpecKind): List<PrimalFeed>? {
        return withContext(dispatcherProvider.io()) {
            val response = feedsApi.getDefaultUserFeeds(specKind = specKind)
            val content = CommonJson.decodeFromStringOrNull<List<ContentPrimalFeedData>>(
                string = response.articleFeeds.content,
            )

            content?.map { it.asFeedPO(ownerId = userId, specKind = specKind) }
                ?.map { it.asPrimalFeedDO() }
        }
    }

    override suspend fun persistRemotelyAllLocalUserFeeds(userId: String) {
        persistRemotelyLocalUserFeedsBySpecKind(userId = userId, specKind = FeedSpecKind.Notes)
        persistRemotelyLocalUserFeedsBySpecKind(userId = userId, specKind = FeedSpecKind.Reads)
    }

    private suspend fun persistRemotelyLocalUserFeedsBySpecKind(userId: String, specKind: FeedSpecKind) =
        withContext(dispatcherProvider.io()) {
            val feeds = database.feeds()
                .getAllFeedsBySpecKind(ownerId = userId, specKind = specKind)
                .map { it.asPrimalFeedDO() }

            val signedEvent = signatureHandler.signNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.ApplicationSpecificData.value,
                    tags = listOf("${AppBuildHelper.getAppName()} App".asIdentifierTag()),
                    content = ContentAppSubSettings(
                        key = specKind.settingsKey,
                        settings = feeds.map { it.asContentPrimalFeedData() },
                    ).encodeToJsonString(),
                ),
            )

            feedsApi.setUserFeeds(userFeedsNostrEvent = signedEvent)
        }

    override suspend fun persistLocallyAndRemotelyUserFeeds(
        userId: String,
        specKind: FeedSpecKind,
        feeds: List<PrimalFeed>,
    ) = withContext(dispatcherProvider.io()) {
        database.withTransaction {
            database.feeds().deleteAllByOwnerIdAndSpecKind(ownerId = userId, specKind = specKind)
            database.feeds().upsertAll(data = feeds.map { it.asFeedPO() })
        }

        val signedEvent = signatureHandler.signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${AppBuildHelper.getAppName()} App".asIdentifierTag()),
                content = ContentAppSubSettings(
                    key = specKind.settingsKey,
                    settings = feeds.map { it.asContentPrimalFeedData() },
                ).encodeToJsonString(),
            ),
        )

        feedsApi.setUserFeeds(userFeedsNostrEvent = signedEvent)
    }

    override suspend fun fetchAndPersistDefaultFeeds(
        userId: String,
        specKind: FeedSpecKind,
        givenDefaultFeeds: List<PrimalFeed>,
    ) = withContext(dispatcherProvider.io()) {
        val feeds = givenDefaultFeeds.ifEmpty {
            fetchDefaultFeeds(userId = userId, specKind = specKind) ?: return@withContext
        }
        persistLocallyAndRemotelyUserFeeds(userId = userId, specKind = specKind, feeds = feeds)
    }

    override suspend fun fetchRecommendedDvmFeeds(userId: String, specKind: FeedSpecKind?): List<DvmFeed> {
        // TODO This looks an api call and should be places in remote-caching
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
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
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
                val dvmMetadata = CommonJson.decodeFromStringOrNull<ContentPrimalDvmFeedMetadata>(nostrEvent.content)
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

    override suspend fun addDvmFeedLocally(
        userId: String,
        dvmFeed: DvmFeed,
        specKind: FeedSpecKind,
    ) {
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

    override suspend fun addFeedLocally(
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

    override suspend fun removeFeedLocally(userId: String, feedSpec: String) {
        withContext(dispatcherProvider.io()) {
            database.feeds().deleteAllByOwnerIdAndSpec(ownerId = userId, spec = feedSpec)
        }
    }

    inline fun <reified T> List<PrimalEvent>.parseAndMapContentByKey(key: T.() -> String): Map<String, T> =
        this.mapNotNull { primalEvent ->
            CommonJson.decodeFromStringOrNull<T>(primalEvent.content)
        }.asMapByKey { it.key() }
}
