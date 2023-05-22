package net.primal.android.nostr.primal.processor

import kotlinx.serialization.decodeFromString
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.Feed
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.primal.model.NostrPrimalEvent
import net.primal.android.nostr.primal.model.response.AppSettingsResponse
import net.primal.android.nostr.primal.model.response.FeedData
import net.primal.android.serialization.NostrJson

class PrimalSettingsProcessor(
    private val database: PrimalDatabase
) : NostrPrimalEventProcessor {

    override val kind = NostrEventKind.PrimalDefaultSettings

    override fun process(events: List<NostrPrimalEvent>) {
        database.feeds().upsertAll(
            data = events
                .map { NostrJson.decodeFromString<AppSettingsResponse>(it.content) }
                .flatMap { it.feeds }
                .map { it.asFeedPO() }
        )
    }

    private fun FeedData.asFeedPO(): Feed = Feed(name = name, hex = hex, pubKey = npub)

}
