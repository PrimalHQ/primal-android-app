package net.primal.android.notifications.api

import net.primal.android.notifications.api.model.NotificationsRequestBody
import net.primal.android.notifications.api.model.NotificationsResponse
import net.primal.android.notifications.domain.NotificationsSummary
import java.time.Instant

interface NotificationsApi {

    suspend fun getLastSeenTimestamp(userId: String): Instant?

    suspend fun updateLastSeenTimestamp(userId: String)

    suspend fun getNotifications(body: NotificationsRequestBody): NotificationsResponse

    suspend fun getNotificationsSummary(userId: String): NotificationsSummary?

}
