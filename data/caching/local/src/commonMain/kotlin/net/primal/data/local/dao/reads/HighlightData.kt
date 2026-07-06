package net.primal.data.local.dao.reads

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    indices = [
        Index(value = ["referencedEventATag"]),
    ],
)
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
