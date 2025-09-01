package net.primal.domain.streams

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.nostr.Naddr

interface StreamRepository {

    suspend fun findLatestLiveStreamATag(mainHostId: String): String?

    suspend fun findWhoIsLive(mainHostIds: List<String>): Set<String>

    fun observeStream(aTag: String): Flow<Stream?>

    suspend fun getStream(aTag: String): Result<Stream>

    suspend fun startLiveStreamSubscription(
        naddr: Naddr,
        userId: String,
        streamContentModerationMode: StreamContentModerationMode,
    )

    suspend fun startLiveEventsFromFollowsSubscription(userId: String)

    fun observeLiveEventsFromFollows(userId: String): Flow<List<Stream>>
}
