package net.primal.android.notifications.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.notifications.api.NotificationsApi
import net.primal.android.notifications.api.mediator.NotificationsRemoteMediator
import net.primal.android.notifications.db.Notification

@OptIn(ExperimentalPagingApi::class)
class NotificationRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
    private val notificationsApi: NotificationsApi,
) {

    fun observeUnseenNotifications() = database.notifications().allUnseenNotifications()

    suspend fun markAllNotificationsAsSeen(userId: String) {
        withContext(dispatcherProvider.io()) {
            val seenAt = Instant.now()
            notificationsApi.setLastSeenTimestamp(userId = userId)
            constructRemoteMediator(userId = userId).updateLastSeenTimestamp(lastSeen = seenAt)
            database.notifications().markAllUnseenNotificationsAsSeen(seenAt = seenAt.epochSecond)
        }
    }

    fun observeSeenNotifications(userId: String): Flow<PagingData<Notification>> {
        return createPager(userId = userId) {
            database.notifications().allSeenNotificationsPaged()
        }.flow
    }

    private fun constructRemoteMediator(userId: String) =
        NotificationsRemoteMediator(
            userId = userId,
            notificationsApi = notificationsApi,
            database = database,
        )

    private fun createPager(userId: String, pagingSourceFactory: () -> PagingSource<Int, Notification>) =
        Pager(
            config = PagingConfig(
                pageSize = 50,
                prefetchDistance = 100,
                initialLoadSize = 200,
                enablePlaceholders = true,
            ),
            remoteMediator = constructRemoteMediator(userId = userId),
            pagingSourceFactory = pagingSourceFactory,
        )
}
