package net.primal.domain.explore

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.UserProfileSearchItem
import net.primal.domain.common.exception.NetworkException

interface ExploreRepository {

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchTrendingZaps(userId: String): List<ExploreZapNoteData>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchTrendingPeople(userId: String): List<ExplorePeopleData>

    fun getFollowLists(): Flow<PagingData<FollowPack>>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchFollowLists(
        since: Long?,
        until: Long?,
        limit: Int = 25,
        offset: Int? = null,
    ): List<FollowPack>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchFollowList(profileId: String, identifier: String): FollowPack?

    fun observeFollowList(profileId: String, identifier: String): Flow<FollowPack?>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchTrendingTopics()

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchPopularUsers(): List<UserProfileSearchItem>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun searchUsers(query: String, limit: Int = 20): List<UserProfileSearchItem>

    fun observeTrendingTopics(): Flow<List<ExploreTrendingTopic>>
}
