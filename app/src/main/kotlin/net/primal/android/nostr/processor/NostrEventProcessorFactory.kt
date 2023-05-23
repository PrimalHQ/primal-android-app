package net.primal.android.nostr.processor

import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.NostrEventKind

class NostrEventProcessorFactory(
    private val database: PrimalDatabase,
) {
    fun create(kind: NostrEventKind): NostrEventProcessor? {
        return when (kind) {
            NostrEventKind.Metadata -> MetadataEventProcessor(database = database)
            NostrEventKind.ShortTextNote -> ShortTextNoteEventProcessor(database = database)
            NostrEventKind.Reposts -> RepostEventProcessor(database = database)
            else -> null
        }
    }

}