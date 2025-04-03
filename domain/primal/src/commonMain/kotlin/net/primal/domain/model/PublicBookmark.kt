package net.primal.domain.model

import net.primal.domain.BookmarkType

data class PublicBookmark(
    val tagValue: String,
    val tagType: String,
    val bookmarkType: BookmarkType,
    val ownerId: String,
)
