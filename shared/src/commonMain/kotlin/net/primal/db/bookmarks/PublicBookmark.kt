package net.primal.db.bookmarks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PublicBookmark(
    @PrimaryKey
    val tagValue: String,
    val tagType: String,
    val bookmarkType: BookmarkType,
    val ownerId: String,
)
