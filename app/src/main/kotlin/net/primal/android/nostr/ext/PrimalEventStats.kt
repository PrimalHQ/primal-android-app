package net.primal.android.nostr.ext

import net.primal.android.feed.db.PostStats
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats

fun ContentPrimalEventStats.asEventStatsPO() = PostStats(
    postId = this.eventId,
    likes = this.likes,
    replies = this.replies,
    mentions = this.mentions,
    reposts = this.reposts,
    zaps = this.zaps,
    satsZapped = this.satsZapped,
    score = this.score,
    score24h = this.score24h,
)