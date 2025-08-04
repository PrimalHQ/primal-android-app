package net.primal.domain.streams

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import net.primal.domain.nostr.Naddr

interface StreamRepository {

    suspend fun findLatestLiveStreamATag(authorId: String): String?

    fun observeStream(aTag: String): Flow<Stream?>

    fun startMonitoring(
        scope: CoroutineScope,
        naddr: Naddr,
        userId: String,
    )

    fun stopMonitoring(scope: CoroutineScope)
}
