package net.primal.android.nostr.ext

import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

fun List<PrimalEvent>.mapNotNullAsPostDataPO(referencedPosts: List<PostData> = emptyList()) =
    this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
        .map { it.asPost(referencedPosts = referencedPosts) }
