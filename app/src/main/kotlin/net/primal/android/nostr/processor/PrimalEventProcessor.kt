package net.primal.android.nostr.processor

import net.primal.android.nostr.model.primal.PrimalEvent

interface PrimalEventProcessor {

    fun process(events: List<PrimalEvent>)

}