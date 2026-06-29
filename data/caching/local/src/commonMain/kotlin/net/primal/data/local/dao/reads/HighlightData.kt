package net.primal.data.local.dao.reads

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
