package net.primal.android.feeds.api

import net.primal.android.feeds.api.model.FeedsResponse

interface FeedsApi {

    suspend fun getFeaturedReadsFeeds(): DvmFeedsResponse

    suspend fun getFeaturedHomeFeeds(): DvmFeedsResponse

    suspend fun getReadsUserFeeds(userId: String): FeedsResponse

    suspend fun setAppSubSettings(userId: String)

    suspend fun getHomeUserFeeds(userId: String): FeedsResponse

    suspend fun setHomeUserFeeds(userId: String)
}
