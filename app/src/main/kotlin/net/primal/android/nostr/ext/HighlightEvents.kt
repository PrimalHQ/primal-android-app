package net.primal.android.nostr.ext

import net.primal.android.highlights.db.HighlightData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent

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
        referencedEventAuthorId = this.tags.findFirstProfileId()?.extractProfileId(),
        createdAt = this.createdAt,
    )
