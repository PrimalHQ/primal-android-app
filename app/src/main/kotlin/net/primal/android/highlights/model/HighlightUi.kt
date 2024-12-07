package net.primal.android.highlights.model

import net.primal.android.highlights.db.HighlightData

data class HighlightUi(
    val highlightId: String,
    val authorId: String,
    val content: String,
    val context: String?,
    val alt: String?,
    val highlightEventId: String?,
    val highlightEventAuthorId: String?,
    val createdAt: Long,
)

fun HighlightData.asHighlightUi() =
    HighlightUi(
        highlightId = highlightId,
        authorId = authorId,
        content = content,
        context = context,
        alt = alt,
        highlightEventId = highlightEventId,
        highlightEventAuthorId = highlightEventAuthorId,
        createdAt = createdAt,
    )
