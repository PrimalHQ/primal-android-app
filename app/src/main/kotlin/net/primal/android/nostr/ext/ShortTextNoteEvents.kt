package net.primal.android.nostr.ext

import net.primal.android.core.utils.parseUrls
import net.primal.android.core.utils.parseNip19
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent

fun List<NostrEvent>.mapNotNullAsPost() = map { it.asPost() }

fun NostrEvent.asPost(): PostData = PostData(
    postId = this.id,
    authorId = this.pubKey,
    createdAt = this.createdAt,
    tags = this.tags,
    content = this.content,
    urls = this.content.parseUrls(),
    nip19Links = this.content.parseNip19(),
    sig = this.sig,
)
