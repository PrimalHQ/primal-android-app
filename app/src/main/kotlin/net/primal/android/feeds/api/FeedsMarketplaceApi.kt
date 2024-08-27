package net.primal.android.feeds.api

interface FeedsMarketplaceApi {

    suspend fun getFeaturedReadsFeeds(): DvmFeedsResponse
}
