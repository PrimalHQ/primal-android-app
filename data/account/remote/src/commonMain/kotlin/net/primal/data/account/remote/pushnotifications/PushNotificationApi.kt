package net.primal.data.account.remote.pushnotifications

import net.primal.domain.nostr.NostrEvent

interface PushNotificationApi {

    suspend fun updateNotificationsToken(authorizationEvents: List<NostrEvent>, token: String)

    suspend fun updateNotificationTokenForNip46(authorizationEvent: NostrEvent, token: String)
}
