package net.primal.android.nostr.processor

import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.model.NostrEventKind

class PrimalEventProcessorFactory(
    private val database: PrimalDatabase,
) {
    fun create(kind: NostrEventKind): PrimalEventProcessor? {
        return when (kind) {
            NostrEventKind.PrimalDefaultSettings -> PrimalSettingsProcessor(database = database)
            NostrEventKind.PrimalEventStats -> PrimalEventStatsProcessor(database = database)
            NostrEventKind.PrimalReferencedEvent -> PrimalReferencedEventProcessor(database = database)
            NostrEventKind.PrimalEventResources -> PrimalResourcesEventProcessor(database = database)
            else -> null
        }
    }
}
