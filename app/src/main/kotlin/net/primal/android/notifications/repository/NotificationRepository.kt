package net.primal.android.notifications.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.notifications.api.NotificationsApi
import net.primal.android.notifications.api.mediator.NotificationsRemoteMediator
import net.primal.android.notifications.db.Notification
import net.primal.android.user.accounts.active.ActiveAccountStore
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class NotificationRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val notificationsApi: NotificationsApi,
    private val activeAccountStore: ActiveAccountStore,
) {

    fun observeUnseenNotifications() = database.notifications().allUnseenNotifications()

    suspend fun markAllNotificationsAsSeen() {
        withContext(Dispatchers.IO) {
            val seenAt = Instant.now()
            val userId = activeAccountStore.activeUserId()
            notificationsApi.setLastSeenTimestamp(userId = userId)
            remoteMediator.updateLastSeenTimestamp(lastSeen = seenAt)
            database.notifications().markAllUnseenNotificationsAsSeen(seenAt = seenAt.epochSecond)
        }
    }

    fun observeSeenNotifications(): Flow<PagingData<Notification>> {
        return createPager {
            database.notifications().allSeenNotificationsPaged()
        }.flow
    }

    private val remoteMediator = NotificationsRemoteMediator(
        userId = activeAccountStore.activeUserId(),
        notificationsApi = notificationsApi,
        database = database,
    )

    private fun createPager(
        pagingSourceFactory: () -> PagingSource<Int, Notification>,
    ) = Pager(
        config = PagingConfig(pageSize = 50, enablePlaceholders = false),
        remoteMediator = remoteMediator,
        pagingSourceFactory = pagingSourceFactory,
    )

}
