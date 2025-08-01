package net.primal.domain.streams

import kotlinx.coroutines.flow.Flow
import net.primal.domain.nostr.NostrEvent

interface StreamRepository {

    suspend fun findLatestLiveStreamATag(authorId: String): String?

    fun observeStream(aTag: String): Flow<Stream?>

    suspend fun saveZap(zapEvent: NostrEvent, zappedEventATag: String)
}
