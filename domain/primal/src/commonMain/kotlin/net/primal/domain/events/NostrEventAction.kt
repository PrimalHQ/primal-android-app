package net.primal.domain.events

import net.primal.domain.nostr.NostrEvent
import net.primal.domain.profile.ProfileData

data class NostrEventAction(
    val profile: ProfileData,
    val score: Float,
    val actionEventData: NostrEvent,
    val actionEventKind: Int,
)
