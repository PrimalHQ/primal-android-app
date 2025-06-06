package net.primal.data.remote.api.notifications

import kotlinx.datetime.Instant
import net.primal.data.remote.api.notifications.model.NotificationsRequestBody
import net.primal.data.remote.api.notifications.model.NotificationsResponse
import net.primal.domain.nostr.NostrEvent

interface NotificationsApi {

    suspend fun getLastSeenTimestamp(userId: String): Instant?

    suspend fun setLastSeenTimestamp(authorization: NostrEvent)

    suspend fun getNotifications(body: NotificationsRequestBody): NotificationsResponse
}
