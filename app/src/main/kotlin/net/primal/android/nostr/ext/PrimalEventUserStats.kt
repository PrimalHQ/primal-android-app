package net.primal.android.nostr.ext

import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats
import net.primal.android.profile.db.PostUserStats
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.decodeFromStringOrNull

fun ContentPrimalEventUserStats.asPostUserStatsPO(userId: String) = PostUserStats(
    postId = this.eventId,
    userId = userId,
    liked = this.liked,
    zapped = this.zapped,
    reposted = this.reposted,
    replied = this.replied,
)

fun List<PrimalEvent>.mapNotNullAsPostUserStatsPO(userId: String) =
    this.mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventUserStats>(it.content) }
        .map { it.asPostUserStatsPO(userId = userId) }
