package net.primal.android.nostr.mappers

import kotlinx.serialization.json.buildJsonArray
import net.primal.android.articles.highlights.HighlightUi
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.domain.ReferencedHighlight

fun HighlightUi.toReferencedHighlight() =
    ReferencedHighlight(
        text = this.content,
        eventId = this.highlightId,
        authorId = this.referencedEventAuthorId,
        aTag = this.referencedEventATag?.asReplaceableEventTag() ?: buildJsonArray { },
    )
