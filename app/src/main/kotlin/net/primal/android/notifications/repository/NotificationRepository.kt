package net.primal.android.notifications.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.notifications.api.mediator.NotificationsRemoteMediator
import net.primal.android.notifications.db.Notification
import net.primal.data.remote.api.notifications.NotificationsApi
import net.primal.domain.nostr.NostrEvent

@OptIn(ExperimentalPagingApi::class)
class NotificationRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val database: PrimalDatabase,
    private val notificationsApi: NotificationsApi,
) {

    fun observeUnseenNotifications(ownerId: String) =
        database.notifications()
            .allUnseenNotifications(ownerId = ownerId)

    suspend fun markAllNotificationsAsSeen(authorization: NostrEvent) {
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

    fun observeSeenNotifications(userId: String): Flow<PagingData<Notification>> {
        return createPager(userId = userId) {
            database.notifications().allSeenNotificationsPaged(ownerId = userId)
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
