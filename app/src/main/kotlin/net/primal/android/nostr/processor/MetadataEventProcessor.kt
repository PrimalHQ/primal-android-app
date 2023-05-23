package net.primal.android.nostr.processor

import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.mapAsProfileMetadata
import net.primal.android.nostr.model.NostrEvent

class MetadataEventProcessor(
    private val database: PrimalDatabase,
) : NostrEventProcessor {

    override fun process(events: List<NostrEvent>) {
        database.profiles().upsertAll(
            events = events.mapAsProfileMetadata()
        )
    }

}