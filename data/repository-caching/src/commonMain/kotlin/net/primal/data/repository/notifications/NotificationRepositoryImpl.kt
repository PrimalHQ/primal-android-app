package net.primal.data.repository.notifications

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.notifications.Notification as NotificationPO
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.notifications.NotificationsApi
import net.primal.data.repository.mappers.local.asNotificationDO
import net.primal.data.repository.notifications.paging.NotificationsRemoteMediator
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.notifications.Notification as NotificationDO
import net.primal.domain.notifications.NotificationRepository

@OptIn(ExperimentalPagingApi::class)
class NotificationRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
    private val notificationsApi: NotificationsApi,
) : NotificationRepository {

    override fun observeUnseenNotifications(ownerId: String) =
        database.notifications().allUnseenNotifications(ownerId = ownerId)
            .map { it.map { it.asNotificationDO() } }

    override suspend fun markAllNotificationsAsSeen(authorization: NostrEvent) {
        withContext(dispatcherProvider.io()) {
            val seenAt = Clock.System.now()
            val userId = authorization.pubKey
            notificationsApi.setLastSeenTimestamp(authorization = authorization)
            constructRemoteMediator(userId = userId).updateLastSeenTimestamp(lastSeen = seenAt)
            database.notifications().markAllUnseenNotificationsAsSeen(
                ownerId = userId,
                seenAt = seenAt.epochSeconds,
            )
        }
    }

    override fun observeSeenNotifications(userId: String): Flow<PagingData<NotificationDO>> {
        return createPager(userId = userId) {
            database.notifications().allSeenNotificationsPaged(ownerId = userId)
        }.flow.map { it.map { it.asNotificationDO() } }
    }

    private fun constructRemoteMediator(userId: String) =
        NotificationsRemoteMediator(
            userId = userId,
            dispatcherProvider = dispatcherProvider,
            notificationsApi = notificationsApi,
            database = database,
        )

    private fun createPager(userId: String, pagingSourceFactory: () -> PagingSource<Int, NotificationPO>) =
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
