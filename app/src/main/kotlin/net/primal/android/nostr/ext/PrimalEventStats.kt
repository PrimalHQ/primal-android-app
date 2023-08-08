package net.primal.android.nostr.ext

import net.primal.android.feed.db.PostStats
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull

fun ContentPrimalEventStats.asPostStatsPO() = PostStats(
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

fun List<PrimalEvent>.mapNotNullAsPostStatsPO() =
    this.mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventStats>(it.content) }
        .map { it.asPostStatsPO() }
