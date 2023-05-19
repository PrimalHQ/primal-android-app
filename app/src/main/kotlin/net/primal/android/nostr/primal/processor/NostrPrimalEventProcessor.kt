package net.primal.android.nostr.primal.processor

import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.primal.model.NostrPrimalEvent

interface NostrPrimalEventProcessor {

    val kind: NostrEventKind

    fun process(events: List<NostrPrimalEvent>)

}