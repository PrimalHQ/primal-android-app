package net.primal.data.repository.articles

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.mappers.local.asHighlightDO
import net.primal.data.repository.mappers.remote.asHighlightData
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asAltTag
import net.primal.domain.nostr.asContextTag
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asKindTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.asReplaceableEventTag
import net.primal.domain.publisher.PrimalPublisher
import net.primal.domain.repository.HighlightRepository

class HighlightRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val primalPublisher: PrimalPublisher,
) : HighlightRepository {

    override fun observeHighlightById(highlightId: String) =
        database.highlights().observeById(highlightId = highlightId)
            .distinctUntilChanged()
            .filterNotNull()
            .map { it.asHighlightDO() }

    override suspend fun publishAndSaveHighlight(
        userId: String,
        content: String,
        referencedEventATag: String?,
        referencedEventAuthorTag: String?,
        context: String?,
        alt: String,
        createdAt: Long,
    ) = withContext(dispatcherProvider.io()) {
        val publishResult = primalPublisher.signPublishImportNostrEvent(
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

    override suspend fun publishDeleteHighlight(userId: String, highlightId: String) =
        withContext(dispatcherProvider.io()) {
            primalPublisher.signPublishImportNostrEvent(
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
