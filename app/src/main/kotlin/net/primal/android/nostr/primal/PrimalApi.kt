package net.primal.android.nostr.primal

import net.primal.android.nostr.primal.model.request.FeedRequest

interface PrimalApi {

    fun requestFeedUpdates(request: FeedRequest)

}