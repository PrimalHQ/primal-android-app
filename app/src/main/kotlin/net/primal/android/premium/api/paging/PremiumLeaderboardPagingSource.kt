package net.primal.android.premium.api.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import net.primal.android.premium.leaderboard.domain.OGLeaderboardEntry
import net.primal.android.premium.repository.PremiumRepository

class PremiumLeaderboardPagingSource(
    private val premiumRepository: PremiumRepository,
    private val pageSize: Int,
) : PagingSource<Long, OGLeaderboardEntry>() {

    override fun getRefreshKey(state: PagingState<Long, OGLeaderboardEntry>): Long? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.premiumSince?.minus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, OGLeaderboardEntry> {
        val currentUntil = params.key

        return try {
            val response = premiumRepository.fetchPremiumLeaderboard(
                until = currentUntil,
                limit = pageSize,
            )

            val nextUntil = if (response.isEmpty()) null else response.last().premiumSince?.minus(1)

            LoadResult.Page(
                data = response,
                prevKey = null,
                nextKey = nextUntil,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
