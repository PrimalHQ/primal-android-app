package net.primal.domain.streams

import kotlinx.coroutines.flow.Flow

interface StreamRepository {

    suspend fun findLatestLiveStreamATag(authorId: String): String?

    fun observeStream(aTag: String): Flow<Stream?>
}
