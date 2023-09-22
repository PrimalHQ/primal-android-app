package net.primal.android.notifications.api

import net.primal.android.notifications.api.model.NotificationsRequestBody
import net.primal.android.notifications.api.model.NotificationsResponse
import java.time.Instant

interface NotificationsApi {

    suspend fun getLastSeenTimestamp(userId: String): Instant?

    suspend fun setLastSeenTimestamp(userId: String)

    suspend fun getNotifications(body: NotificationsRequestBody): NotificationsResponse

}
