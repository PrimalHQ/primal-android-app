package net.primal.android.nostr.processor

import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.mapNotNullAsRepost
import net.primal.android.nostr.model.NostrEvent

class RepostEventProcessor(
    private val database: PrimalDatabase,
) : NostrEventProcessor {

    override fun process(events: List<NostrEvent>) {
        database.reposts().upsertAll(
            data = events.mapNotNullAsRepost()
        )
    }

}
