package net.primal.android.nostr.ext

import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.toJsonObject
import net.primal.android.core.utils.parseHashtags
import net.primal.android.core.utils.parseUris
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent

fun List<NostrEvent>.mapAsPostDataPO() = map { it.asPost() }

fun NostrEvent.asPost(): PostData =
    PostData(
        postId = this.id,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        tags = this.tags,
        content = this.content,
        uris = this.content.parseUris(),
        hashtags = this.parseHashtags(),
        sig = this.sig,
        raw = NostrJson.encodeToString(this.toJsonObject()),
    )
