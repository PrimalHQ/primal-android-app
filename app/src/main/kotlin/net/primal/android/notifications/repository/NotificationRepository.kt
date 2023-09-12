package net.primal.android.notifications.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.model.FeedResponse
import net.primal.android.feed.repository.persistToDatabaseAsTransaction
import net.primal.android.nostr.ext.mapNotNullAsNotificationPO
import net.primal.android.nostr.ext.mapNotNullAsProfileStatsPO
import net.primal.android.notifications.api.NotificationsApi
import net.primal.android.notifications.domain.NotificationsSummary
import java.time.Instant
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val notificationsApi: NotificationsApi,
) {

    fun observeNotifications() = database.notifications().allSortedByCreatedAtDesc()

    suspend fun deleteNotifications(userId: String) {
        withContext(Dispatchers.IO) {
            database.notifications().deleteAlL(userId = userId)
        }
    }

    suspend fun fetchNotifications(userId: String) = withContext(Dispatchers.IO) {
        val response = notificationsApi.getNotifications(userId = userId)
        FeedResponse(
            paging = null,
            metadata = response.metadata,
            posts = response.notes,
            reposts = emptyList(),
            referencedPosts = response.primalReferencedNotes,
            primalEventStats = response.primalNoteStats,
            primalEventUserStats = emptyList(),
            primalEventResources = response.primalMediaResources,
        ).persistToDatabaseAsTransaction(
            userId = userId,
            database = database
        )

        val userProfileStats = response.primalUserProfileStats.mapNotNullAsProfileStatsPO()
        val notifications = response.primalNotifications.mapNotNullAsNotificationPO()

        database.withTransaction {
            database.profileStats().upsertAll(data = userProfileStats)
            database.notifications().upsertAll(data = notifications)
        }
    }

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
