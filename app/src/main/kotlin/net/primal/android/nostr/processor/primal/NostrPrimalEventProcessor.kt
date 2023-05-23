package net.primal.android.nostr.processor.primal

import net.primal.android.nostr.model.primal.NostrPrimalEvent

interface NostrPrimalEventProcessor {

    fun process(events: List<NostrPrimalEvent>)

}