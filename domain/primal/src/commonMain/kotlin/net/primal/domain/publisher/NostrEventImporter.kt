package net.primal.domain.publisher

import net.primal.domain.nostr.NostrEvent

interface NostrEventImporter {

    suspend fun importEvents(events: List<NostrEvent>): Boolean
}
