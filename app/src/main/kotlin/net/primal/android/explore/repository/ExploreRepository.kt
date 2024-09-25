package net.primal.android.explore.repository

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.explore.api.ExploreApi
import net.primal.android.explore.api.model.SearchUsersRequestBody
import net.primal.android.explore.api.model.TopicScore
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.explore.db.TrendingTopic
import net.primal.android.explore.domain.UserProfileSearchItem
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.takeContentAsPrimalUserScoresOrNull
import net.primal.android.profile.db.ProfileStats

class ExploreRepository @Inject constructor(
    private val exploreApi: ExploreApi,
    private val database: PrimalDatabase,
) {

    fun observeTrendingTopics() = database.trendingTopics().allSortedByScore()

    suspend fun fetchTrendingTopics() {
        val response = exploreApi.getTrendingTopics()
        val topics = response.map { it.asTrendingTopicPO() }

        if (topics.isNotEmpty()) {
            database.withTransaction {
                database.trendingTopics().deleteAll()
                database.trendingTopics().upsertAll(data = topics)
            }
        }
    }

    private fun TopicScore.asTrendingTopicPO() = TrendingTopic(topic = this.name, score = this.score)

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> {
        val response = apiBlock()
        val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profiles = response.contactsMetadata.mapAsProfileDataPO(cdnResources = cdnResources)
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

        return result
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
