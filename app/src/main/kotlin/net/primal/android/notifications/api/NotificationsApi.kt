package net.primal.android.notifications.api

import java.time.Instant
import net.primal.android.notifications.api.model.NotificationsRequestBody
import net.primal.android.notifications.api.model.NotificationsResponse

interface NotificationsApi {

    suspend fun getLastSeenTimestamp(userId: String): Instant?

    suspend fun setLastSeenTimestamp(userId: String)

    suspend fun getNotifications(body: NotificationsRequestBody): NotificationsResponse
}
