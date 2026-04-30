package net.primal.domain.notifications

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent

interface NotificationRepository {

    fun observeUnseenNotifications(ownerId: String, group: NotificationGroup): Flow<List<Notification>>

    fun observeSeenNotifications(userId: String, group: NotificationGroup): Flow<PagingData<Notification>>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun markAllNotificationsAsSeen(authorization: NostrEvent)
}
