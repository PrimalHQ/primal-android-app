package net.primal.android.nostr.ext

import net.primal.android.events.db.EventStats
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
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
    this.mapNotNull { it.content.decodeFromJsonStringOrNull<ContentPrimalEventStats>() }
        .map { it.asEventStatsPO() }
