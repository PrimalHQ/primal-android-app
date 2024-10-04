package net.primal.android.bookmarks.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.android.bookmarks.domain.BookmarkType

@Entity
data class PublicBookmark(
    @PrimaryKey
    val tagValue: String,
    val tagType: String,
    val bookmarkType: BookmarkType,
    val ownerId: String,
)
