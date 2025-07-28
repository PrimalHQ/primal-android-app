package net.primal.domain.streams

import kotlinx.coroutines.flow.Flow

interface StreamRepository {

    fun observeStream(authorId: String): Flow<Stream?>
}
