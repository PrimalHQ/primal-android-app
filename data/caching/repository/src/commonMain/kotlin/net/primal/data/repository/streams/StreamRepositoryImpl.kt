package net.primal.data.repository.streams

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.mappers.local.asStreamDO
import net.primal.domain.streams.Stream
import net.primal.domain.streams.StreamRepository

class StreamRepositoryImpl(
    private val database: PrimalDatabase,
) : StreamRepository {

    override suspend fun findLatestLiveStreamATag(authorId: String): String? {
        val streamsPO = database.streams().observeStreamsByAuthorId(authorId).first()
        val liveStreamPO = streamsPO.find { it.data.isLive() }
        return liveStreamPO?.data?.aTag
    }

    override fun observeStream(aTag: String): Flow<Stream?> {
        return database.streams().observeStreamByATag(aTag = aTag)
            .map { streamPO ->
                streamPO?.asStreamDO()
            }
    }
}
