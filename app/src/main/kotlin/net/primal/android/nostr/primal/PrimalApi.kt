package net.primal.android.nostr.primal

import net.primal.android.nostr.primal.model.request.FeedRequest
import net.primal.android.nostr.primal.model.request.SearchContentRequest

interface PrimalApi {

    fun requestDefaultAppSettings()

    fun requestFeedUpdates(request: FeedRequest)

    fun searchContent(request: SearchContentRequest)
}