package net.primal.data.remote.mapper

import net.primal.core.utils.decodeFromStringOrNull
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.remote.model.ContentPrimalEventUserStats
import net.primal.data.serialization.NostrJson
import net.primal.domain.PrimalEvent

fun ContentPrimalEventUserStats.asEventUserStatsPO(userId: String) =
    EventUserStats(
        eventId = this.eventId,
        userId = userId,
        liked = this.liked,
        zapped = this.zapped,
        reposted = this.reposted,
        replied = this.replied,
    )

fun List<PrimalEvent>.mapNotNullAsEventUserStatsPO(userId: String) =
    this.mapNotNull { NostrJson.decodeFromStringOrNull<ContentPrimalEventUserStats>(it.content) }
        .map { it.asEventUserStatsPO(userId = userId) }
