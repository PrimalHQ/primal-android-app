package net.primal.android.events.domain

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.profile.db.ProfileData

data class EventAction(
    val profile: ProfileData,
    val score: Float,
    val actionEventData: NostrEvent,
    val actionEventKind: Int,
)
