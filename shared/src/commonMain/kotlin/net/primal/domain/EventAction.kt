package net.primal.domain

import net.primal.db.profiles.ProfileData
import net.primal.networking.model.NostrEvent

data class EventAction(
    val profile: ProfileData,
    val score: Float,
    val actionEventData: NostrEvent,
    val actionEventKind: Int,
)
