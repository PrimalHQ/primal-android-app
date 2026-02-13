package net.primal.data.account.remote.pushnotifications

import net.primal.data.account.remote.pushnotifications.model.NotificationScope
import net.primal.domain.nostr.NostrEvent

interface PushNotificationApi {

    suspend fun updateNotificationsToken(authorizationEvents: List<NostrEvent>, token: String)

    suspend fun updateNotificationTokenForRemoteSigners(
        authorizationEvents: List<NostrEvent>,
        token: String,
        scope: NotificationScope = NotificationScope.Nip46,
    )
}
