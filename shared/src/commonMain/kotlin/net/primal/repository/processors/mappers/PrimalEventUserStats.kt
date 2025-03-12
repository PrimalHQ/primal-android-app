package net.primal.repository.processors.mappers

import net.primal.db.events.EventUserStats
import net.primal.networking.model.primal.PrimalEvent
import net.primal.networking.model.primal.content.ContentPrimalEventUserStats
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.decodeFromStringOrNull


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
