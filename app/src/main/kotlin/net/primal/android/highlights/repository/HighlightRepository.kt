package net.primal.android.highlights.repository

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.asAltTag
import net.primal.android.nostr.ext.asContextTag
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asHighlightData
import net.primal.android.nostr.ext.asKindTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.nostr.utils.Nevent
import net.primal.domain.nostr.NostrEventKind

class HighlightRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val dispatchers: CoroutineDispatcherProvider,
    private val nostrPublisher: NostrPublisher,
) {
    companion object {
        const val DEFAULT_ALT_TAG =
            "This is a highlight created in https://primal.net Android application"
    }

    fun observeHighlightById(highlightId: String) =
        database.highlights().observeById(highlightId = highlightId)
            .distinctUntilChanged()
            .filterNotNull()

    suspend fun publishAndSaveHighlight(
        userId: String,
        content: String,
        referencedEventATag: String?,
        referencedEventAuthorTag: String?,
        context: String?,
        alt: String = DEFAULT_ALT_TAG,
        createdAt: Long = Instant.now().epochSecond,
    ) = withContext(dispatchers.io()) {
        val publishResult = nostrPublisher.signPublishImportNostrEvent(
            userId = userId,
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.Highlight.value,
                content = content,
                createdAt = createdAt,
                tags = listOfNotNull(
                    referencedEventATag?.asReplaceableEventTag(),
                    referencedEventAuthorTag?.asPubkeyTag(),
                    context?.asContextTag(),
                    alt.asAltTag(),
                ),
            ),
        )
        val highlightNostrEvent = publishResult.nostrEvent
        val highlightData = highlightNostrEvent.asHighlightData()
        database.highlights().upsert(highlightData)
        Nevent(
            kind = NostrEventKind.Highlight.value,
            userId = highlightData.authorId,
            eventId = highlightData.highlightId,
        )
    }

    suspend fun publishDeleteHighlight(userId: String, highlightId: String) =
        withContext(dispatchers.io()) {
            nostrPublisher.signPublishImportNostrEvent(
                userId = userId,
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.EventDeletion.value,
                    content = "",
                    tags = listOf(
                        highlightId.asEventIdTag(),
                        NostrEventKind.Highlight.asKindTag(),
                    ),
                ),
            )

            database.highlights().deleteById(highlightId = highlightId)
        }
}
