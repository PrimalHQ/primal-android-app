package net.primal.android.nostr.processor.primal

import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.NostrEventKind

class NostrPrimalEventProcessorFactory(
    private val database: PrimalDatabase,
) {
    fun create(kind: NostrEventKind): NostrPrimalEventProcessor? {
        return when (kind) {
            NostrEventKind.PrimalDefaultSettings -> PrimalSettingsProcessor(database = database)
            NostrEventKind.PrimalEventStats -> PrimalEventStatsProcessor(database = database)
            NostrEventKind.PrimalReferencedEvent -> PrimalReferencedEventProcessor(database = database)
            else -> null
        }
    }
}
