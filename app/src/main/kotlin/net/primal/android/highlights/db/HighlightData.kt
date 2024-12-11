package net.primal.android.highlights.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HighlightData(
    @PrimaryKey
    val highlightId: String,
    val authorId: String,
    val content: String,
    val context: String?,
    val alt: String?,
    val referencedEventATag: String?,
    val referencedEventAuthorId: String?,
    val createdAt: Long,
)
