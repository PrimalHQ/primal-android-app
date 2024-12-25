package net.primal.android.bookmarks

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.bookmarks.db.PublicBookmark
import net.primal.android.bookmarks.domain.BookmarkType
import net.primal.android.bookmarks.domain.TagBookmark
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.api.UsersApi

class BookmarksRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
    private val nostrPublisher: NostrPublisher,
    private val usersApi: UsersApi,
) {

    private suspend fun fetchLatestPublicBookmarks(userId: String): Set<TagBookmark>? {
        val bookmarksResponse = withContext(dispatcherProvider.io()) {
            usersApi.getUserBookmarksList(userId = userId)
        }
        return bookmarksResponse.bookmarksListEvent?.tags?.parseAsPublicBookmarks()
    }

    suspend fun fetchAndPersistPublicBookmarks(userId: String) {
        val bookmarks = fetchLatestPublicBookmarks(userId = userId)
        persistUserBookmarks(userId = userId, bookmarks = bookmarks)
    }

    private suspend fun persistUserBookmarks(userId: String, bookmarks: Set<TagBookmark>?) {
        withContext(dispatcherProvider.io()) {
            val bookmarksDao = database.publicBookmarks()
            val notesBookmarks = bookmarks?.filter { it.type == "e" }?.map {
                PublicBookmark(
                    ownerId = userId,
                    bookmarkType = BookmarkType.Note,
                    tagType = it.type,
                    tagValue = it.value,
                )
            } ?: emptyList()

            val articleBookmarks = bookmarks?.filter { it.type == "a" }?.mapNotNull {
                val kind = it.value.split(":").getOrNull(index = 0)?.toIntOrNull()
                if (kind == NostrEventKind.LongFormContent.value) {
                    PublicBookmark(
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

    suspend fun isBookmarked(tagValue: String) =
        withContext(dispatcherProvider.io()) {
            database.publicBookmarks().findByTagValue(tagValue = tagValue) != null
        }

    @Throws(BookmarksListNotFound::class, NostrPublishException::class)
    suspend fun addToBookmarks(
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
                PublicBookmark(
                    ownerId = userId,
                    bookmarkType = BookmarkType.Article,
                    tagType = tagType,
                    tagValue = tagValue,
                ),
            ),
        )
    }

    @Throws(BookmarksListNotFound::class, NostrPublishException::class)
    suspend fun removeFromBookmarks(
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

    @Throws(BookmarksListNotFound::class, NostrPublishException::class)
    private suspend fun publishAddBookmark(
        userId: String,
        forceUpdate: Boolean,
        bookmark: TagBookmark,
    ) {
        publishBookmarksList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { add(bookmark) }
        }
    }

    @Throws(BookmarksListNotFound::class, NostrPublishException::class)
    private suspend fun publishRemoveBookmark(
        userId: String,
        forceUpdate: Boolean,
        bookmark: TagBookmark,
    ) {
        publishBookmarksList(userId = userId, forceUpdate = forceUpdate) {
            toMutableSet().apply { remove(bookmark) }
        }
    }

    @Throws(BookmarksListNotFound::class, NostrPublishException::class)
    private suspend fun publishBookmarksList(
        userId: String,
        forceUpdate: Boolean,
        reducer: Set<TagBookmark>.() -> Set<TagBookmark>,
    ) {
        val latestBookmarks = fetchLatestPublicBookmarks(userId = userId)
            ?: if (forceUpdate) emptySet() else throw BookmarksListNotFound()

        val updatedBookmarks = latestBookmarks.reducer()

        withContext(dispatcherProvider.io()) {
            val bookmarksTags = updatedBookmarks.map {
                buildJsonArray {
                    add(it.type)
                    add(it.value)
                }
            }
            nostrPublisher.signPublishImportNostrEvent(
                userId = userId,
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

    class BookmarksListNotFound : Exception()
}
