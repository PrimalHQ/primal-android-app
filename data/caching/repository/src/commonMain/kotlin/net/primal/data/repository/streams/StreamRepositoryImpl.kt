package net.primal.data.repository.streams

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.mappers.local.asStreamDO
import net.primal.domain.streams.Stream
import net.primal.domain.streams.StreamRepository

class StreamRepositoryImpl(
    private val database: PrimalDatabase,
) : StreamRepository {

    override fun observeStream(authorId: String): Flow<Stream?> {
        return database.streams().observeStream(authorId = authorId).map {
            it?.asStreamDO()
        }
    }
}
