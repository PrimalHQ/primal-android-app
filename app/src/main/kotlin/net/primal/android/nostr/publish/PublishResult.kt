package net.primal.android.nostr.publish

import net.primal.domain.nostr.NostrEvent

data class PublishResult(
    val nostrEvent: NostrEvent,
    val imported: Boolean,
)
