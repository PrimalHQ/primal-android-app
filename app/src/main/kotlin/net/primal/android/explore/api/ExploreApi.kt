package net.primal.android.explore.api

import net.primal.android.explore.api.model.ExploreRequestBody
import net.primal.android.explore.api.model.SearchUsersRequestBody
import net.primal.android.explore.api.model.TopicScore
import net.primal.android.explore.api.model.TrendingPeopleResponse
import net.primal.android.explore.api.model.UsersResponse

interface ExploreApi {

    suspend fun getTrendingPeople(body: ExploreRequestBody): TrendingPeopleResponse

    suspend fun getTrendingTopics(): List<TopicScore>

    suspend fun getPopularUsers(): UsersResponse

    suspend fun searchUsers(body: SearchUsersRequestBody): UsersResponse
}
