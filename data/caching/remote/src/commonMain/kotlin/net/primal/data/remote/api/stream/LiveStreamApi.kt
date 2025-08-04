package net.primal.data.remote.api.stream

import kotlinx.coroutines.flow.Flow
import net.primal.data.remote.api.stream.model.LiveFeedResponse
import net.primal.domain.nostr.Naddr

interface LiveStreamApi {

    suspend fun subscribe(streamingNaddr: Naddr, userId: String): Flow<LiveFeedResponse>
}
