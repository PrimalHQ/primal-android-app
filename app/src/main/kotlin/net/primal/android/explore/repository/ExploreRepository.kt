package net.primal.android.explore.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.explore.api.ExploreApi
import net.primal.android.explore.api.model.HashtagScore
import net.primal.android.explore.api.model.SearchUsersRequestBody
import net.primal.android.explore.api.model.UsersResponse
import net.primal.android.explore.db.TrendingHashtag
import net.primal.android.nostr.ext.mapAsProfileMetadataPO
import net.primal.android.nostr.ext.takeContentAsPrimalUserScoresOrNull
import javax.inject.Inject

class ExploreRepository @Inject constructor(
    private val exploreApi: ExploreApi,
    private val database: PrimalDatabase,
) {

    fun observeTrendingHashtags() = database.trendingHashtags().allSortedByScore()

    suspend fun fetchTrendingHashtags() {
        val response = exploreApi.getTrendingHashtags()

        database.withTransaction {
            database.trendingHashtags().deleteAlL()
            database.trendingHashtags().upsertAll(data = response.map { it.asTrendingHashtagPO() })
        }
    }

    private fun HashtagScore.asTrendingHashtagPO() =
        TrendingHashtag(hashtag = this.name, score = this.score)

    private suspend fun queryRemoteUsers(apiBlock: suspend () -> UsersResponse): List<UserProfileSearchItem> {
        val response = apiBlock()
        val profiles = response.contactsMetadata.mapAsProfileMetadataPO()
        val userScoresMap = response.userScores?.takeContentAsPrimalUserScoresOrNull()

        withContext(Dispatchers.IO) {
            database.profiles().upsertAll(profiles = profiles)
        }

        return profiles.map {
            val score = userScoresMap?.get(it.ownerId)
            UserProfileSearchItem(metadata = it, score = score)
        }.sortedByDescending { it.score }
    }

    suspend fun searchUsers(query: String) = queryRemoteUsers {
        exploreApi.searchUsers(SearchUsersRequestBody(query = query, limit = 10))
    }

    suspend fun getRecommendedUsers() = queryRemoteUsers {
        exploreApi.getRecommendedUsers()
    }
}
