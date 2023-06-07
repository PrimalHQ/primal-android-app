package net.primal.android.nostr.processor

import kotlinx.serialization.decodeFromString
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Feed
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.nostr.model.primal.content.ContentFeedData
import net.primal.android.serialization.NostrJson

class PrimalSettingsProcessor(
    private val database: PrimalDatabase
) : PrimalEventProcessor {

    override fun process(events: List<PrimalEvent>) {
        database.feeds().upsertAll(
            data = events
                .map { NostrJson.decodeFromString<ContentAppSettings>(it.content) }
                .flatMap { it.feeds }
                .map { it.asFeedPO() }
        )
    }

    private fun ContentFeedData.asFeedPO(): Feed = Feed(name = name, directive = directive)

}
