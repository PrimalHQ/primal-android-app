package net.primal.data.remote.api.notifications.model

import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent

data class NotificationsResponse(
    val metadata: List<NostrEvent>,
    val notes: List<NostrEvent>,
    val primalNoteStats: List<PrimalEvent>,
    val primalUserProfileStats: List<PrimalEvent>,
    val primalReferencedNotes: List<PrimalEvent>,
    val primalNotifications: List<PrimalEvent>,
    val cdnResources: List<PrimalEvent>,
    val primalLinkPreviews: List<PrimalEvent>,
    val primalRelayHints: List<PrimalEvent>,
    val blossomServers: List<NostrEvent>,
    val primalUserNames: PrimalEvent? = null,
    val primalLegendProfiles: PrimalEvent? = null,
    val primalPremiumInfo: PrimalEvent? = null,
)
