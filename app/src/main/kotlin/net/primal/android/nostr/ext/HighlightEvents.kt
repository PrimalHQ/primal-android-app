package net.primal.android.nostr.ext

import net.primal.android.highlights.db.HighlightData
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

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
