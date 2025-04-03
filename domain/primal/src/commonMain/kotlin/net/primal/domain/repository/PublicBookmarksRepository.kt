package net.primal.domain.repository

import kotlin.coroutines.cancellation.CancellationException
import net.primal.domain.BookmarkType
import net.primal.domain.nostr.PublicBookmarksNotFoundException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.NostrPublishException

interface PublicBookmarksRepository {

    suspend fun fetchAndPersistBookmarks(userId: String)

    suspend fun isBookmarked(tagValue: String): Boolean

    @Throws(
        PublicBookmarksNotFoundException::class,
        NostrPublishException::class,
        SigningRejectedException::class,
        SigningKeyNotFoundException::class,
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
        SigningRejectedException::class,
        SigningKeyNotFoundException::class,
        CancellationException::class,
    )
    suspend fun removeFromBookmarks(
        userId: String,
        bookmarkType: BookmarkType,
        tagValue: String,
        forceUpdate: Boolean,
    )
}
