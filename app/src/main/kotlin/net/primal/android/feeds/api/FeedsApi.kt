package net.primal.android.feeds.api

import net.primal.android.feeds.api.model.FeedsResponse
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.nostr.model.primal.content.ContentArticleFeedData

interface FeedsApi {

    suspend fun getFeaturedFeeds(specKind: FeedSpecKind): DvmFeedsResponse

    suspend fun getDefaultUserFeeds(specKind: FeedSpecKind): FeedsResponse

    suspend fun getUserFeeds(userId: String, specKind: FeedSpecKind): FeedsResponse

    suspend fun setUserFeeds(
        userId: String,
        specKind: FeedSpecKind,
        feeds: List<ContentArticleFeedData>,
    )
}
