package net.primal.android.notifications.api.model

import net.primal.android.nostr.model.NostrEvent

data class NotificationsResponse(
    val metadata: List<NostrEvent>,
    val posts: List<NostrEvent>,
)