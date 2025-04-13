package net.primal.domain.bookmarks

data class PublicBookmark(
    val tagValue: String,
    val tagType: String,
    val bookmarkType: BookmarkType,
    val ownerId: String,
)
