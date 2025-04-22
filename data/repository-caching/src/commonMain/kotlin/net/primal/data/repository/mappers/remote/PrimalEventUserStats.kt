package net.primal.data.repository.mappers.remote

import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.remote.model.ContentPrimalEventUserStats
import net.primal.domain.common.PrimalEvent

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
    this.mapNotNull { it.content.decodeFromJsonStringOrNull<ContentPrimalEventUserStats>() }
        .map { it.asEventUserStatsPO(userId = userId) }
