package net.primal.data.repository.mappers.remote

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.dao.events.EventStats
import net.primal.data.remote.model.ContentPrimalEventStats
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
