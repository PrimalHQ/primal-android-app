package net.primal.android.notes.db

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.highlights.model.HighlightUi
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.parseEventTags

@Serializable
data class ReferencedHighlight(
    val text: String,
    val eventId: String?,
    val authorId: String?,
    val aTag: JsonArray,
)

fun HighlightUi.toReferencedHighlight() =
    ReferencedHighlight(
        text = this.content,
        eventId = this.highlightId,
        authorId = this.referencedEventAuthorId,
        aTag = this.referencedEventATag?.asReplaceableEventTag() ?: buildJsonArray {  },
    )
