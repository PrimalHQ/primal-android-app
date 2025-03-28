package net.primal.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import net.primal.domain.model.Highlight
import net.primal.domain.nostr.Nevent

interface HighlightRepository {

    private companion object {
        const val DEFAULT_ALT_TAG =
            "This is a highlight created in https://primal.net Android application"
    }

    fun observeHighlightById(highlightId: String): Flow<Highlight>

    suspend fun publishAndSaveHighlight(
        userId: String,
        content: String,
        referencedEventATag: String?,
        referencedEventAuthorTag: String?,
        context: String?,
        alt: String = DEFAULT_ALT_TAG,
        createdAt: Long = Clock.System.now().epochSeconds,
    ): Nevent

    suspend fun publishDeleteHighlight(userId: String, highlightId: String)
}
