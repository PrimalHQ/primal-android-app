package net.primal.repository.processors.mappers

import net.primal.db.reads.HighlightData
import net.primal.networking.model.NostrEvent
import net.primal.networking.model.NostrEventKind
import net.primal.networking.model.primal.PrimalEvent
import net.primal.repository.findFirstAltDescription
import net.primal.repository.findFirstContextTag
import net.primal.repository.findFirstProfileId
import net.primal.repository.findFirstReplaceableEventId

fun List<PrimalEvent>.mapReferencedEventsAsHighlightDataPO() =
    this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
        .filter { it.kind == NostrEventKind.Highlight.value }
        .map { it.asHighlightData() }

fun NostrEvent.asHighlightData() =
    HighlightData(
        highlightId = this.id,
        authorId = this.pubKey,
        content = this.content,
        alt = this.tags.findFirstAltDescription(),
        context = this.tags.findFirstContextTag(),
        referencedEventATag = this.tags.findFirstReplaceableEventId(),
        referencedEventAuthorId = this.tags.findFirstProfileId(),
        createdAt = this.createdAt,
    )
