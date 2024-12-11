package net.primal.android.highlights.model

import net.primal.android.highlights.db.HighlightData

data class HighlightUi(
    val highlightId: String,
    val authorId: String,
    val content: String,
    val context: String?,
    val alt: String?,
    val referencedEventATag: String?,
    val referencedEventAuthorId: String?,
    val createdAt: Long,
)

fun HighlightData.asHighlightUi() =
    HighlightUi(
        highlightId = highlightId,
        authorId = authorId,
        content = content,
        context = context,
        alt = alt,
        referencedEventATag = referencedEventATag,
        referencedEventAuthorId = referencedEventAuthorId,
        createdAt = createdAt,
    )
