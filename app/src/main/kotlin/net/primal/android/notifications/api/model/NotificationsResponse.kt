package net.primal.android.notifications.api.model

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

data class NotificationsResponse(
    val metadata: List<NostrEvent>,
    val notes: List<NostrEvent>,
    val primalNoteStats: List<PrimalEvent>,
    val primalUserProfileStats: List<PrimalEvent>,
    val primalReferencedNotes: List<PrimalEvent>,
    val primalNotifications: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val primalLinkPreviews: List<PrimalEvent>,
)
