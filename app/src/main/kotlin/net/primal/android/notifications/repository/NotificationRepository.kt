package net.primal.android.notifications.repository

import net.primal.android.notifications.api.NotificationsApi
import net.primal.android.notifications.domain.NotificationsSummary
import java.time.Instant
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationsApi: NotificationsApi,
) {

    suspend fun fetchLastSeenTimestamp(userId: String): Instant? {
        return notificationsApi.getLastSeenTimestamp(userId = userId)
    }

    suspend fun updateLastSeenTimestamp(userId: String) {
        notificationsApi.updateLastSeenTimestamp(userId = userId)
    }

    suspend fun fetchNotificationsSummary(userId: String): NotificationsSummary? {
        return notificationsApi.getNotificationsSummary(userId = userId)
    }
}
