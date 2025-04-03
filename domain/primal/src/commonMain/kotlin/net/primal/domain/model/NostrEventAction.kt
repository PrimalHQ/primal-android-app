package net.primal.domain.model

import net.primal.domain.nostr.NostrEvent

data class NostrEventAction(
    val profile: ProfileData,
    val score: Float,
    val actionEventData: NostrEvent,
    val actionEventKind: Int,
)
