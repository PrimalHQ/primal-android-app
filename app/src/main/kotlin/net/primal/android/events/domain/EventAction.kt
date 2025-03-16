package net.primal.android.events.domain

import net.primal.android.profile.db.ProfileData
import net.primal.domain.nostr.NostrEvent

data class EventAction(
    val profile: ProfileData,
    val score: Float,
    val actionEventData: NostrEvent,
    val actionEventKind: Int,
)
