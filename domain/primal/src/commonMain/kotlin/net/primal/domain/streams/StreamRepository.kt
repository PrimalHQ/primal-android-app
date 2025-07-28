package net.primal.domain.streams

import kotlinx.coroutines.flow.Flow

interface StreamRepository {

    suspend fun findLatestLiveStream(authorId: String): Stream?

    fun observeStream(aTag: String): Flow<Stream?>
}
