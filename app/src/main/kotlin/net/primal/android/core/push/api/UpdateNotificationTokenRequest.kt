package net.primal.android.core.push.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class UpdateNotificationTokenRequest(
    @SerialName("events_from_users") val authorizationEvents: List<NostrEvent>,
    val platform: String,
    val token: String,
)
