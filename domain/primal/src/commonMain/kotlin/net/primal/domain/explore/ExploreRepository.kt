package net.primal.domain.explore

import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.UserProfileSearchItem

interface ExploreRepository {

    suspend fun fetchTrendingZaps(userId: String): List<ExploreZapNoteData>

    suspend fun fetchTrendingPeople(userId: String): List<ExplorePeopleData>

    suspend fun fetchTrendingTopics()

    suspend fun fetchPopularUsers(): List<UserProfileSearchItem>

    suspend fun searchUsers(query: String, limit: Int = 20): List<UserProfileSearchItem>

    fun observeTrendingTopics(): Flow<List<ExploreTrendingTopic>>
}
