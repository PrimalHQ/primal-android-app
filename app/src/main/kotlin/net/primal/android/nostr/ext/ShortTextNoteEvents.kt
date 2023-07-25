package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.core.utils.parseUrls
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.serialization.NostrJson

fun List<NostrEvent>.mapNotNullAsPost() = map { it.asPost() }

fun NostrEvent.asPost(): PostData = PostData(
    postId = this.id,
    authorId = this.pubKey,
    createdAt = this.createdAt,
    tags = this.tags,
    content = this.content,
    urls = this.content.parseUrls(),
    sig = this.sig,
    raw = NostrJson.encodeToString(this),
)
