package net.primal.android.user.domain

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.user.accounts.parseFollowings
import net.primal.android.user.accounts.parseInterests

fun NostrEvent.asUserAccountFromFollowListEvent() =
    UserAccount(
        pubkey = pubKey,
        authorDisplayName = pubKey.asEllipsizedNpub(),
        userDisplayName = pubKey.asEllipsizedNpub(),
        following = tags.parseFollowings(),
        interests = tags.parseInterests(),
        followListEventContent = content,
    )

fun NostrEvent.asUserAccountFromBookmarksListEvent() =
    UserAccount(
        pubkey = pubKey,
        authorDisplayName = pubKey.asEllipsizedNpub(),
        userDisplayName = pubKey.asEllipsizedNpub(),
        bookmarks = tags.parseAsPublicBookmarks(),
    )

private fun List<JsonArray>.parseAsPublicBookmarks(): Set<PublicBookmark> {
    val bookmarks = mutableSetOf<PublicBookmark>()
    this.forEach {
        val type = it.getOrNull(0)?.jsonPrimitive?.content
        val value = it.getOrNull(1)?.jsonPrimitive?.content
        if (type != null && value != null) {
            bookmarks.add(PublicBookmark(type = type, value = value))
        }
    }
    return bookmarks
}
