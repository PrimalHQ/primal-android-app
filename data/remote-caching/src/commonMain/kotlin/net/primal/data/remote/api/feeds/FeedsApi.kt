package net.primal.data.remote.api.feeds

import net.primal.data.remote.api.feeds.model.DvmFeedsResponse
import net.primal.data.remote.api.feeds.model.FeedsResponse
import net.primal.domain.FeedSpecKind
import net.primal.domain.nostr.NostrEvent

interface FeedsApi {

    suspend fun getFeaturedFeeds(specKind: FeedSpecKind? = null, pubkey: String? = null): DvmFeedsResponse

    suspend fun getDefaultUserFeeds(specKind: FeedSpecKind): FeedsResponse

    suspend fun getUserFeeds(authorization: NostrEvent, specKind: FeedSpecKind): FeedsResponse

    suspend fun setUserFeeds(userFeedsNostrEvent: NostrEvent)
}
