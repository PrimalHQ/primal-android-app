package net.primal.android.nostr.ext

import net.primal.android.events.db.EventUserStats
import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
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
    this.mapNotNull { CommonJson.decodeFromStringOrNull<ContentPrimalEventUserStats>(it.content) }
        .map { it.asEventUserStatsPO(userId = userId) }
