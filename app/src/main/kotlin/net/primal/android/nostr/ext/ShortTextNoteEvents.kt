package net.primal.android.nostr.ext

import net.primal.android.core.utils.parseUris
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent

fun List<NostrEvent>.mapNotNullAsPost() = map { it.asPost() }

fun NostrEvent.asPost(): PostData = PostData(
    postId = this.id,
    authorId = this.pubKey,
    createdAt = this.createdAt,
    tags = this.tags,
    content = this.content,
    uris = this.content.parseUris(),
    sig = this.sig,
)
