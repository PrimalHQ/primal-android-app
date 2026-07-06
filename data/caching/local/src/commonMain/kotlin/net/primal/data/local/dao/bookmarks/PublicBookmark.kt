package net.primal.data.local.dao.bookmarks

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.domain.bookmarks.BookmarkType

@Entity
data class PublicBookmark(
    @PrimaryKey
    val tagValue: String,
    val tagType: String,
    val bookmarkType: BookmarkType,
    val ownerId: String,
)
