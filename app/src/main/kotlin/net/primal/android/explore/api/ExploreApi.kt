package net.primal.android.explore.api

import net.primal.android.explore.api.model.HashtagScore

interface ExploreApi {

    suspend fun getTrendingHashtags(): List<HashtagScore>

}
