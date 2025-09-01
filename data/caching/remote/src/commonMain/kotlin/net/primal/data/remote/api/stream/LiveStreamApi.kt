package net.primal.data.remote.api.stream

import kotlinx.coroutines.flow.Flow
import net.primal.data.remote.api.stream.model.LiveFeedResponse
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEvent

interface LiveStreamApi {

    suspend fun subscribeToLiveEvent(
        streamingNaddr: Naddr,
        userId: String,
        contentModerationMode: String,
    ): Flow<LiveFeedResponse>

    suspend fun subscribeToLiveEventsFromFollows(userId: String): Flow<NostrEvent>
}
