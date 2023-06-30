package net.primal.android.nostr.ext

import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats
import net.primal.android.profile.db.PostUserStats

fun ContentPrimalEventUserStats.asEventUserStatsPO(userId: String) = PostUserStats(
    postId = this.eventId,
    userId = userId,
    liked = this.liked,
    zapped = this.zapped,
    reposted = this.reposted,
    replied = this.replied,
)
