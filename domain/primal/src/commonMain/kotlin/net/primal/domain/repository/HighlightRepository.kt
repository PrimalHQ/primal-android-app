package net.primal.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import net.primal.domain.model.Highlight
import net.primal.domain.nostr.Nevent

interface HighlightRepository {

    fun observeHighlightById(highlightId: String): Flow<Highlight>

    suspend fun publishAndSaveHighlight(
        userId: String,
        content: String,
        referencedEventATag: String?,
        referencedEventAuthorTag: String?,
        context: String?,
        alt: String,
        createdAt: Long = Clock.System.now().epochSeconds,
    ): Nevent

    suspend fun publishDeleteHighlight(userId: String, highlightId: String)
}
