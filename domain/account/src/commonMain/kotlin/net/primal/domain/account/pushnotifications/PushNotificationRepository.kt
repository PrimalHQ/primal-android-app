package net.primal.domain.account.pushnotifications

import net.primal.domain.nostr.NostrEvent

interface PushNotificationRepository {

    suspend fun updateNotificationsToken(authorizationEvents: List<NostrEvent>, token: String)

    suspend fun updateNotificationTokenForNip46(authorizationEvent: NostrEvent, token: String)
}
