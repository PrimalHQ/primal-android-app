package net.primal.domain.repository

import kotlin.coroutines.cancellation.CancellationException
import net.primal.domain.BookmarkType
import net.primal.domain.error.NetworkException
import net.primal.domain.nostr.PublicBookmarksNotFoundException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.NostrPublishException

interface PublicBookmarksRepository {

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchAndPersistBookmarks(userId: String)

    suspend fun isBookmarked(tagValue: String): Boolean

    @Throws(
        PublicBookmarksNotFoundException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
    )
    suspend fun addToBookmarks(
        userId: String,
        bookmarkType: BookmarkType,
        tagValue: String,
        forceUpdate: Boolean,
    )

    @Throws(
        PublicBookmarksNotFoundException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
    )
    suspend fun removeFromBookmarks(
        userId: String,
        bookmarkType: BookmarkType,
        tagValue: String,
        forceUpdate: Boolean,
    )
}
