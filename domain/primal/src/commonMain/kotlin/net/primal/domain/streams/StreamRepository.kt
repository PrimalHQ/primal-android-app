package net.primal.domain.streams

import kotlinx.coroutines.flow.Flow
import net.primal.domain.nostr.Naddr

interface StreamRepository {

    suspend fun findLatestLiveStreamATag(authorId: String): String?

    suspend fun findWhoIsLive(authorIds: List<String>): Set<String>

    fun observeStream(aTag: String): Flow<Stream?>

    suspend fun startLiveStreamSubscription(naddr: Naddr, userId: String)
}
