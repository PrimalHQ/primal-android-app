package net.primal.android.explore.api

import net.primal.android.explore.api.model.HashtagScore
import net.primal.android.explore.api.model.SearchUserRequestBody
import net.primal.android.explore.api.model.SearchUserResponse

interface ExploreApi {

    suspend fun getTrendingHashtags(): List<HashtagScore>

    suspend fun searchUsers(body: SearchUserRequestBody): SearchUserResponse

}
