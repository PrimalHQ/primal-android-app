package net.primal.android.explore.repository

import androidx.room.withTransaction
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.explore.api.ExploreApi
import net.primal.android.explore.api.model.ExplorePeopleData
import net.primal.android.explore.api.model.ExploreRequestBody
import net.primal.android.explore.api.model.SearchUsersRequestBody
import net.primal.android.explore.api.model.TopicScore
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.explore.db.TrendingTopic
import net.primal.android.explore.domain.ExploreZapNoteData
import net.primal.android.explore.domain.UserProfileSearchItem
import net.primal.android.networking.primal.retryNetworkCall
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.flatMapNotNullAsLinkPreviewResource
import net.primal.android.nostr.ext.flatMapNotNullAsVideoThumbnailsMap
import net.primal.android.nostr.ext.flatMapPostsAsNoteNostrUriPO
import net.primal.android.nostr.ext.mapAsEventZapDO
import net.primal.android.nostr.ext.mapAsPostDataPO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.ext.takeContentAsPrimalUserFollowStats
import net.primal.android.nostr.ext.takeContentAsPrimalUserFollowersCountsOrNull
import net.primal.android.nostr.ext.takeContentAsPrimalUserScoresOrNull
import net.primal.android.profile.db.ProfileStats
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

class ExploreRepository @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val exploreApi: ExploreApi,
    private val database: PrimalDatabase,
) {

    suspend fun fetchTrendingZaps(userId: String): List<ExploreZapNoteData> =
        withContext(dispatchers.io()) {
            val response = retryNetworkCall {
                exploreApi.getTrendingZaps(body = ExploreRequestBody(userPubKey = userId, limit = 100))
            }

            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val videoThumbnails = response.cdnResources.flatMapNotNullAsVideoThumbnailsMap()
            val linkPreviews = response.primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url }

            val profiles = response.metadata.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalLegendProfiles = primalLegendProfiles,
            )

            val profilesMap = profiles.associateBy { it.ownerId }

            val eventZaps = response.nostrZapEvents.mapAsEventZapDO(profilesMap = profilesMap)

            val notes = response.noteEvents.mapAsPostDataPO(referencedPosts = emptyList())
            val nostrUris = notes.flatMapPostsAsNoteNostrUriPO(
                postIdToPostDataMap = emptyMap(),
                articleIdToArticle = emptyMap(),
                profileIdToProfileDataMap = profilesMap,
                cdnResources = cdnResources,
                videoThumbnails = videoThumbnails,
                linkPreviews = linkPreviews,
            )

            database.withTransaction {
                database.profiles().upsertAll(data = profiles)
                database.eventZaps().upsertAll(data = eventZaps)
            }

            val notesMap = notes.associateBy { it.postId }

            eventZaps.mapNotNull { zapEvent ->
                notesMap[zapEvent.eventId]?.let { noteData ->
                    ExploreZapNoteData(
                        sender = profilesMap[zapEvent.zapSenderId],
                        receiver = profilesMap[zapEvent.zapReceiverId],
                        noteData = noteData,
                        amountSats = zapEvent.amountInBtc.toBigDecimal().toSats(),
                        zapMessage = zapEvent.message,
                        createdAt = Instant.ofEpochSecond(zapEvent.zapReceiptAt),
                        noteNostrUris = nostrUris,
                    )
                }
            }.sortedByDescending { it.amountSats }
        }

    suspend fun fetchTrendingPeople(userId: String): List<ExplorePeopleData> =
        withContext(dispatchers.io()) {
            val response = retryNetworkCall {
                exploreApi.getTrendingPeople(body = ExploreRequestBody(userPubKey = userId, limit = 100))
            }

            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val profiles = response.metadata.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalLegendProfiles = primalLegendProfiles,
            )
            val userScoresMap = response.usersScores?.takeContentAsPrimalUserScoresOrNull()
            val usersFollowStats = response.usersFollowStats?.takeContentAsPrimalUserFollowStats()
            val userFollowCount = response.usersFollowCount?.takeContentAsPrimalUserFollowersCountsOrNull()

            database.withTransaction {
                database.profiles().upsertAll(data = profiles)
            }

            profiles.map {
                ExplorePeopleData(
                    profile = it.asProfileDetailsUi(),
                    userScore = userScoresMap?.get(it.ownerId) ?: 0f,
                    userFollowersCount = userFollowCount?.get(it.ownerId) ?: 0,
                    followersIncrease = usersFollowStats?.get(it.ownerId)?.increase ?: 0,
                    verifiedFollowersCount = usersFollowStats?.get(it.ownerId)?.count ?: 0,
                )
            }.sortedBy {
                response.paging?.elements?.indexOf(it.profile.pubkey)
            }
        }

    fun observeTrendingTopics() = database.trendingTopics().allSortedByScore()

    suspend fun fetchTrendingTopics() =
        withContext(dispatchers.io()) {
            val response = retryNetworkCall { exploreApi.getTrendingTopics() }
            val topics = response.map { it.asTrendingTopicPO() }

            if (topics.isNotEmpty()) {
                database.withTransaction {
                    database.trendingTopics().deleteAll()
                    database.trendingTopics().upsertAll(data = topics)
                }
            }
        }

    private fun TopicScore.asTrendingTopicPO() = TrendingTopic(topic = this.name, score = this.score)

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> =
        withContext(dispatchers.io()) {
            val response = retryNetworkCall { apiBlock() }
            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
            val profiles = response.contactsMetadata.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalLegendProfiles = primalLegendProfiles,
            )
            val userScoresMap = response.userScores?.takeContentAsPrimalUserScoresOrNull()
            val result = profiles.map {
                val score = userScoresMap?.get(it.ownerId)
                UserProfileSearchItem(metadata = it, score = score, followersCount = score?.toInt())
            }.sortedByDescending { it.score }

            database.withTransaction {
                database.profiles().upsertAll(data = profiles)
                database.profileStats().insertOrIgnore(
                    data = result.map {
                        ProfileStats(profileId = it.metadata.ownerId, followers = it.followersCount)
                    },
                )
            }

            result
        }

    suspend fun searchUsers(query: String, limit: Int = 20) =
        queryRemoteUsers {
            exploreApi.searchUsers(SearchUsersRequestBody(query = query, limit = limit))
        }

    suspend fun fetchPopularUsers() =
        queryRemoteUsers {
            exploreApi.getPopularUsers()
        }

    fun observeRecentUsers(): Flow<List<UserProfileSearchItem>> {
        return database.profileInteractions().observeRecentProfiles()
            .map { recentProfiles ->
                recentProfiles.mapNotNull { profile ->
                    if (profile.metadata != null) {
                        UserProfileSearchItem(
                            metadata = profile.metadata,
                            followersCount = profile.stats?.followers,
                        )
                    } else {
                        null
                    }
                }
            }
    }
}
