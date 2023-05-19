package net.primal.android.nostr.primal.processor

import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.primal.model.NostrPrimalEvent

class PrimalReferencedEventProcessor : NostrPrimalEventProcessor {

    override val kind = NostrEventKind.PrimalReferencedEvent

    override fun process(events: List<NostrPrimalEvent>) {

    }
}