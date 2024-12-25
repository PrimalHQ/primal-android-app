package net.primal.android.nostr.publish

import net.primal.android.nostr.model.NostrEvent

data class PublishResult(
    val nostrEvent: NostrEvent,
    val imported: Boolean,
)
