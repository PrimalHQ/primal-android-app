package net.primal.android.nostr.processor

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind

interface NostrEventProcessor {

    val kind: NostrEventKind

    fun process(events: List<NostrEvent>)

}