package net.primal.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.domain.model.Notification
import net.primal.domain.nostr.NostrEvent

interface NotificationRepository {

    fun observeUnseenNotifications(ownerId: String): Flow<List<Notification>>

    fun observeSeenNotifications(userId: String): Flow<PagingData<Notification>>

    suspend fun markAllNotificationsAsSeen(authorization: NostrEvent)
}
