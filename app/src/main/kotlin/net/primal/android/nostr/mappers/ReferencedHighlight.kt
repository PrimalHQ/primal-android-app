package net.primal.android.nostr.mappers

import kotlinx.serialization.json.buildJsonArray
import net.primal.android.articles.highlights.HighlightUi
import net.primal.domain.ReferencedHighlight
import net.primal.domain.nostr.asReplaceableEventTag

fun HighlightUi.toReferencedHighlight() =
    ReferencedHighlight(
        text = this.content,
        eventId = this.highlightId,
        authorId = this.referencedEventAuthorId,
        aTag = this.referencedEventATag?.asReplaceableEventTag() ?: buildJsonArray { },
    )
