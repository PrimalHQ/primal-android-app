package net.primal.android.nostr.ext

import net.primal.android.core.utils.parseUris
import kotlinx.serialization.encodeToString
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.serialization.NostrJson

fun List<NostrEvent>.mapNotNullAsPost() = map { it.asPost() }

fun NostrEvent.asPost(): PostData = PostData(
    postId = this.id,
    authorId = this.pubKey,
    createdAt = this.createdAt,
    tags = this.tags ?: emptyList(),
    content = this.content,
    uris = this.content.parseUris(),
    sig = this.sig,
    raw = NostrJson.encodeToString(this),
)
