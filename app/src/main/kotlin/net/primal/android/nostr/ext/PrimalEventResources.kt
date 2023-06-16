package net.primal.android.nostr.ext

import net.primal.android.feed.db.PostResource
import net.primal.android.nostr.model.primal.content.EventResource

fun EventResource.asPostResourcePO(postId: String) = PostResource(
    postId = postId,
    contentType = this.mimeType,
    url = this.url,
    variants = this.variants,
)