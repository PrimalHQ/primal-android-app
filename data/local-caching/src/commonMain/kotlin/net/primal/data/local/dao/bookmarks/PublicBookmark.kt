package net.primal.data.local.dao.bookmarks

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.bookmarks.BookmarkType

@Entity
data class PublicBookmark(
    @PrimaryKey
    val tagValue: String,
    val tagType: String,
    val bookmarkType: BookmarkType,
    val ownerId: String,
)
