package net.primal.data.repository.bookmarks

import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.bookmarks.PublicBookmark as PublicBookmarkPO
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.users.UsersApi
import net.primal.domain.bookmarks.BookmarkType
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.bookmarks.TagBookmark
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.PublicBookmarksNotFoundException
import net.primal.domain.publisher.PrimalPublisher

class PublicBookmarksRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val primalPublisher: PrimalPublisher,
    private val usersApi: UsersApi,
) : PublicBookmarksRepository {

    private suspend fun fetchLatestPublicBookmarks(userId: String): Set<TagBookmark>? {
        val bookmarksResponse = withContext(dispatcherProvider.io()) {
            usersApi.getUserBookmarksList(userId = userId)
        }
        return bookmarksResponse.bookmarksListEvent?.tags?.parseAsPublicBookmarks()
    }

    override suspend fun fetchAndPersistBookmarks(userId: String) {
        val bookmarks = fetchLatestPublicBookmarks(userId = userId)
        persistUserBookmarks(userId = userId, bookmarks = bookmarks)
    }

    private suspend fun persistUserBookmarks(userId: String, bookmarks: Set<TagBookmark>?) {
        withContext(dispatcherProvider.io()) {
            val bookmarksDao = database.publicBookmarks()
            val notesBookmarks = bookmarks?.filter { it.type == "e" }?.map {
                PublicBookmarkPO(
                    ownerId = userId,
                    bookmarkType = BookmarkType.Note,
                    tagType = it.type,
                    tagValue = it.value,
                )
            } ?: emptyList()

            val articleBookmarks = bookmarks?.filter { it.type == "a" }?.mapNotNull {
                val kind = it.value.split(":").getOrNull(index = 0)?.toIntOrNull()
                if (kind == NostrEventKind.LongFormContent.value) {
                    PublicBookmarkPO(
                        ownerId = userId,
                        bookmarkType = BookmarkType.Article,
                        tagType = it.type,
                        tagValue = it.value,
                    )
                } else {
                    null
                }
            } ?: emptyList()

            database.withTransaction {
                bookmarksDao.deleteAllBookmarks(userId = userId)
                bookmarksDao.upsertBookmarks(data = notesBookmarks + articleBookmarks)
            }
        }
    }

    override suspend fun isBookmarked(tagValue: String) =
        withContext(dispatcherProvider.io()) {
            database.publicBookmarks().findByTagValue(tagValue = tagValue) != null
        }

    override suspend fun addToBookmarks(
        userId: String,
        bookmarkType: BookmarkType,
        tagValue: String,
        forceUpdate: Boolean,
    ) = withContext(dispatcherProvider.io()) {
        val tagType = bookmarkType.toTagType()
        publishAddBookmark(
            userId = userId,
            bookmark = TagBookmark(type = tagType, value = tagValue),
            forceUpdate = forceUpdate,
        )

        database.publicBookmarks().upsertBookmarks(
            data = listOf(
                PublicBookmarkPO(
                    ownerId = userId,
                    bookmarkType = BookmarkType.Article,
                    tagType = tagType,
                    tagValue = tagValue,
                ),
            ),
        )
    }

    override suspend fun removeFromBookmarks(
        userId: String,
        bookmarkType: BookmarkType,
        tagValue: String,
        forceUpdate: Boolean,
    ) = withContext(dispatcherProvider.io()) {
        publishRemoveBookmark(
            userId = userId,
            bookmark = TagBookmark(type = bookmarkType.toTagType(), value = tagValue),
            forceUpdate = forceUpdate,
        )

        database.publicBookmarks().deleteByTagValue(tagValue = tagValue)
    }

    private fun BookmarkType.toTagType() =
        when (this) {
            BookmarkType.Note -> "e"
            BookmarkType.Article -> "a"
        }

    private suspend fun publishAddBookmark(
        userId: String,
        forceUpdate: Boolean,
        bookmark: TagBookmark,
    ) {
        publishBookmarksList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { add(bookmark) }
        }
    }

    private suspend fun publishRemoveBookmark(
        userId: String,
        forceUpdate: Boolean,
        bookmark: TagBookmark,
    ) {
        publishBookmarksList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { remove(bookmark) }
        }
    }

    private suspend fun publishBookmarksList(
        userId: String,
        forceUpdate: Boolean,
        reducer: Set<TagBookmark>.() -> Set<TagBookmark>,
    ) {
        val latestBookmarks = fetchLatestPublicBookmarks(userId = userId)
            ?: if (forceUpdate) emptySet() else throw PublicBookmarksNotFoundException()

        val updatedBookmarks = latestBookmarks.reducer()

        withContext(dispatcherProvider.io()) {
            val bookmarksTags = updatedBookmarks.map {
                buildJsonArray {
                    add(it.type)
                    add(it.value)
                }
            }
            primalPublisher.signPublishImportNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.BookmarksList.value,
                    content = "",
                    tags = bookmarksTags,
                ),
            )
        }

        persistUserBookmarks(userId = userId, bookmarks = updatedBookmarks)
    }

    private fun List<JsonArray>.parseAsPublicBookmarks(): Set<TagBookmark> {
        return mapNotNull {
            val type = it.getOrNull(0)?.jsonPrimitive?.content
            val value = it.getOrNull(1)?.jsonPrimitive?.content
            if (type != null && value != null) {
                TagBookmark(type = type, value = value)
            } else {
                null
            }
        }.toSet()
    }
}
