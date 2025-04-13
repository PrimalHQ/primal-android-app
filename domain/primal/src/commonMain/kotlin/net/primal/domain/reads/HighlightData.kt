package net.primal.domain.reads

data class HighlightData(
    val highlightId: String,
    val authorId: String,
    val content: String,
    val context: String?,
    val alt: String?,
    val referencedEventATag: String?,
    val referencedEventAuthorId: String?,
    val createdAt: Long,
)
