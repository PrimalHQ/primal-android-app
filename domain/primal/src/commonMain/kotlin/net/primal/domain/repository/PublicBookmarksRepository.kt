package net.primal.domain.repository

import net.primal.domain.BookmarkType

interface PublicBookmarksRepository {

    suspend fun fetchAndPersistBookmarks(userId: String)

    suspend fun isBookmarked(tagValue: String): Boolean

    suspend fun addToBookmarks(
        userId: String,
        bookmarkType: BookmarkType,
        tagValue: String,
        forceUpdate: Boolean,
    )

    suspend fun removeFromBookmarks(
        userId: String,
        bookmarkType: BookmarkType,
        tagValue: String,
        forceUpdate: Boolean,
    )
}
