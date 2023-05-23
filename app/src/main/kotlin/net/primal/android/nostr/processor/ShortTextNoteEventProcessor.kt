package net.primal.android.nostr.processor

import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.mapNotNullAsPost
import net.primal.android.nostr.model.NostrEvent

class ShortTextNoteEventProcessor(
    private val database: PrimalDatabase,
) : NostrEventProcessor {

    override fun process(events: List<NostrEvent>) {
        database.posts().upsertAll(
            data = events.mapNotNullAsPost()
        )
    }

}
