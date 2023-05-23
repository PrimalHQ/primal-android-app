package net.primal.android.nostr.processor

import net.primal.android.nostr.model.NostrEvent

interface NostrEventProcessor {

    fun process(events: List<NostrEvent>)

}
