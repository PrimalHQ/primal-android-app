package net.primal.android.explore.repository

import androidx.room.withTransaction
import net.primal.android.db.PrimalDatabase
import net.primal.android.explore.api.ExploreApi
import net.primal.android.explore.api.model.HashtagScore
import net.primal.android.explore.db.TrendingHashtag
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
}
