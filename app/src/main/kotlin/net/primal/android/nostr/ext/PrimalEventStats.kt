package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.events.db.EventStats
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.domain.PrimalEvent

fun ContentPrimalEventStats.asEventStatsPO() =
    EventStats(
        eventId = this.eventId,
        likes = this.likes,
        replies = this.replies,
        mentions = this.mentions,
        reposts = this.reposts,
        zaps = this.zaps,
        satsZapped = this.satsZapped,
        score = this.score,
        score24h = this.score24h,
    )

fun List<PrimalEvent>.mapNotNullAsEventStatsPO() =
    this.mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventStats>(it.content) }
        .map { it.asEventStatsPO() }
