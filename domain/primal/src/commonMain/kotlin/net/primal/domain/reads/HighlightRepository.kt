package net.primal.domain.reads

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.NostrPublishException

interface HighlightRepository {

    private companion object {
        const val DEFAULT_ALT_TAG =
            "This is a highlight created in https://primal.net Android application"
    }

    fun observeHighlightById(highlightId: String): Flow<Highlight>

    @Throws(
        NostrPublishException::class,
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun publishAndSaveHighlight(
        userId: String,
        content: String,
        referencedEventATag: String?,
        referencedEventAuthorTag: String?,
        context: String?,
        alt: String = DEFAULT_ALT_TAG,
        createdAt: Long = Clock.System.now().epochSeconds,
    ): Nevent

    @Throws(
        NostrPublishException::class,
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun publishDeleteHighlight(userId: String, highlightId: String)
}
