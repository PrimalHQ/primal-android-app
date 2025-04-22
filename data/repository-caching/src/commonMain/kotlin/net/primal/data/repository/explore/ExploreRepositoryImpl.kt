package net.primal.data.repository.explore

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.asMapByKey
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.explore.ExploreApi
import net.primal.data.remote.api.explore.model.ExploreRequestBody
import net.primal.data.remote.api.explore.model.SearchUsersRequestBody
import net.primal.data.remote.api.explore.model.UsersResponse
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.flatMapNotNullAsLinkPreviewResource
import net.primal.data.remote.mapper.flatMapNotNullAsVideoThumbnailsMap
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asExploreTrendingTopic
import net.primal.data.repository.mappers.local.asProfileDataDO
import net.primal.data.repository.mappers.local.asProfileStatsPO
import net.primal.data.repository.mappers.local.mapAsFeedPostDO
import net.primal.data.repository.mappers.remote.asTrendingTopicPO
import net.primal.data.repository.mappers.remote.flatMapPostsAsReferencedNostrUriDO
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.data.repository.mappers.remote.mapAsPostDataPO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserFollowStats
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserFollowersCountsOrNull
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserScoresOrNull
import net.primal.domain.common.UserProfileSearchItem
import net.primal.domain.explore.ExplorePeopleData
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.explore.ExploreZapNoteData

class ExploreRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val exploreApi: ExploreApi,
    private val database: PrimalDatabase,
) : ExploreRepository {

    override suspend fun fetchTrendingZaps(userId: String): List<ExploreZapNoteData> =
        withContext(dispatcherProvider.io()) {
            val response = retryNetworkCall {
                exploreApi.getTrendingZaps(
                    body = ExploreRequestBody(
                        userPubKey = userId,
                        limit = 100,
                    ),
                )
            }

            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val videoThumbnails = response.cdnResources.flatMapNotNullAsVideoThumbnailsMap()
            val linkPreviews = response.primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url }
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

            val profiles = response.metadata.mapAsProfileDataPO(
                cdnResourcesMap = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )

            val profilesMap = profiles.associateBy { it.ownerId }

            val eventZaps = response.nostrZapEvents.mapAsEventZapDO(profilesMap = profilesMap)

            val notes = response.noteEvents.mapAsPostDataPO(
                referencedPosts = emptyList(),
                referencedArticles = emptyList(),
                referencedHighlights = emptyList(),
            )

            val nostrUris = notes.flatMapPostsAsReferencedNostrUriDO(
                eventIdToNostrEvent = emptyMap(),
                postIdToPostDataMap = emptyMap(),
                articleIdToArticle = emptyMap(),
                profileIdToProfileDataMap = profilesMap,
                cdnResources = cdnResources,
                videoThumbnails = videoThumbnails,
                linkPreviews = linkPreviews,
            )

            database.withTransaction {
                database.profiles().insertOrUpdateAll(data = profiles)
                database.eventZaps().upsertAll(data = eventZaps)
            }

            val notesMap = notes.associateBy { it.postId }

            eventZaps.mapNotNull { zapEvent ->
                notesMap[zapEvent.eventId]?.let { noteData ->
                    ExploreZapNoteData(
                        sender = profilesMap[zapEvent.zapSenderId]?.asProfileDataDO(),
                        receiver = profilesMap[zapEvent.zapReceiverId]?.asProfileDataDO(),
                        noteData = noteData.mapAsFeedPostDO(),
                        amountSats = zapEvent.amountInBtc.toBigDecimal().toSats(),
                        zapMessage = zapEvent.message,
                        createdAt = Instant.fromEpochSeconds(zapEvent.zapReceiptAt),
                        noteNostrUris = nostrUris.filter { it.eventId == noteData.postId },
                    )
                }
            }.sortedByDescending { it.amountSats }
        }

    override suspend fun fetchTrendingPeople(userId: String): List<ExplorePeopleData> =
        withContext(dispatcherProvider.io()) {
            val response = retryNetworkCall {
                exploreApi.getTrendingPeople(
                    body = ExploreRequestBody(
                        userPubKey = userId,
                        limit = 100,
                    ),
                )
            }

            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profiles = response.metadata.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )
            val userScoresMap = response.usersScores?.takeContentAsPrimalUserScoresOrNull()
            val usersFollowStats = response.usersFollowStats?.takeContentAsPrimalUserFollowStats()
            val userFollowCount = response.usersFollowCount?.takeContentAsPrimalUserFollowersCountsOrNull()

            database.withTransaction {
                database.profiles().insertOrUpdateAll(data = profiles)
            }

            profiles.map {
                ExplorePeopleData(
                    profile = it.asProfileDataDO(),
                    userScore = userScoresMap?.get(it.ownerId) ?: 0f,
                    userFollowersCount = userFollowCount?.get(it.ownerId) ?: 0,
                    followersIncrease = usersFollowStats?.get(it.ownerId)?.increase ?: 0,
                    verifiedFollowersCount = usersFollowStats?.get(it.ownerId)?.count ?: 0,
                )
            }.sortedBy {
                response.paging?.elements?.indexOf(it.profile.profileId)
            }
        }

    override fun observeTrendingTopics() =
        database.trendingTopics().allSortedByScore()
            .map { it.map { it.asExploreTrendingTopic() } }

    override suspend fun fetchTrendingTopics() =
        withContext(dispatcherProvider.io()) {
            val response = retryNetworkCall { exploreApi.getTrendingTopics() }
            val topics = response.map { it.asTrendingTopicPO() }

            if (topics.isNotEmpty()) {
                database.withTransaction {
                    database.trendingTopics().deleteAll()
                    database.trendingTopics().upsertAll(data = topics)
                }
            }
        }

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> =
        withContext(dispatcherProvider.io()) {
            val response = retryNetworkCall { apiBlock() }
            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
            val profiles = response.contactsMetadata.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            )
            val userScoresMap = response.userScores?.takeContentAsPrimalUserScoresOrNull()
            val result = profiles.map {
                val score = userScoresMap?.get(it.ownerId)
                UserProfileSearchItem(
                    metadata = it.asProfileDataDO(),
                    score = score,
                    followersCount = score?.toInt(),
                )
            }.sortedByDescending { it.score }

            database.withTransaction {
                database.profiles().insertOrUpdateAll(data = profiles)
                database.profileStats().insertOrIgnore(data = result.map { it.asProfileStatsPO() })
            }

            result
        }

    override suspend fun searchUsers(query: String, limit: Int) =
        queryRemoteUsers {
            exploreApi.searchUsers(SearchUsersRequestBody(query = query, limit = limit))
        }

    override suspend fun fetchPopularUsers() =
        queryRemoteUsers {
            exploreApi.getPopularUsers()
        }
}
