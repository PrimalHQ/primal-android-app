package net.primal.domain.repository

import androidx.paging.PagingData
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.error.NetworkException
import net.primal.domain.model.Notification
import net.primal.domain.nostr.NostrEvent

interface NotificationRepository {

    fun observeUnseenNotifications(ownerId: String): Flow<List<Notification>>

    fun observeSeenNotifications(userId: String): Flow<PagingData<Notification>>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun markAllNotificationsAsSeen(authorization: NostrEvent)
}
