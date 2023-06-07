package net.primal.android.nostr.processor

import kotlinx.serialization.decodeFromString
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.PostStats
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentPrimalEventStats
import net.primal.android.serialization.NostrJson

class PrimalEventStatsProcessor(
    private val database: PrimalDatabase
) : PrimalEventProcessor {

    override suspend fun process(events: List<PrimalEvent>) {
        database.eventStats().upsertAll(
            data = events
                .map { NostrJson.decodeFromString<ContentPrimalEventStats>(it.content) }
                .map { it.asEventStatsPO() }
        )
    }

    private fun ContentPrimalEventStats.asEventStatsPO() = PostStats(
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
}
