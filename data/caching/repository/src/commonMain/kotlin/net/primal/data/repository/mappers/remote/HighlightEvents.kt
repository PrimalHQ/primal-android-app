package net.primal.data.repository.mappers.remote

import net.primal.data.local.dao.reads.HighlightData
import net.primal.domain.common.PrimalEvent
import net.primal.domain.common.util.takeContentOrNull
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findFirstAltDescription
import net.primal.domain.nostr.findFirstContextTag
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.findFirstReplaceableEventId

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
