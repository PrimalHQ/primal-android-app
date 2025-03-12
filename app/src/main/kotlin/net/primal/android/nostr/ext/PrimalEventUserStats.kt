package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.events.db.EventUserStats
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventUserStats

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
