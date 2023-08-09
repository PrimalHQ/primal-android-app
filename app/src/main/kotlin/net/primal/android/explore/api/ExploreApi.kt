package net.primal.android.explore.api

import net.primal.android.explore.api.model.HashtagScore
import net.primal.android.explore.api.model.SearchUsersRequestBody
import net.primal.android.explore.api.model.UsersResponse

interface ExploreApi {

    suspend fun getTrendingHashtags(): List<HashtagScore>

    suspend fun getRecommendedUsers(): UsersResponse

    suspend fun searchUsers(body: SearchUsersRequestBody): UsersResponse

}
