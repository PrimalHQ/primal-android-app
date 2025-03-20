package net.primal.data.remote.api.explore

import net.primal.data.remote.api.explore.model.ExploreRequestBody
import net.primal.data.remote.api.explore.model.SearchUsersRequestBody
import net.primal.data.remote.api.explore.model.TopicScore
import net.primal.data.remote.api.explore.model.TrendingPeopleResponse
import net.primal.data.remote.api.explore.model.TrendingZapsResponse
import net.primal.data.remote.api.explore.model.UsersResponse

interface ExploreApi {

    suspend fun getTrendingPeople(body: ExploreRequestBody): TrendingPeopleResponse

    suspend fun getTrendingZaps(body: ExploreRequestBody): TrendingZapsResponse

    suspend fun getTrendingTopics(): List<TopicScore>

    suspend fun getPopularUsers(): UsersResponse

    suspend fun searchUsers(body: SearchUsersRequestBody): UsersResponse
}
