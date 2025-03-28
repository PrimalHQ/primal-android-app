package net.primal.data.repository.notifications.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.notifications.Notification
import net.primal.data.local.dao.notifications.NotificationData
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.remote.api.notifications.NotificationsApi
import net.primal.data.remote.api.notifications.model.NotificationsRequestBody
import net.primal.data.repository.feed.processors.persistToDatabaseAsTransaction
import net.primal.data.repository.mappers.remote.mapNotNullAsNotificationPO
import net.primal.data.repository.mappers.remote.mapNotNullAsProfileStatsPO

@ExperimentalPagingApi
class NotificationsRemoteMediator(
    private val userId: String,
    private val dispatcherProvider: DispatcherProvider,
    private val notificationsApi: NotificationsApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, Notification>() {

    private var lastSeenTimestamp: Long = Instant.DISTANT_PAST.epochSeconds

    private val lastRequests: MutableMap<LoadType, NotificationsRequestBody> = mutableMapOf()

    fun updateLastSeenTimestamp(lastSeen: Instant) {
        lastSeenTimestamp = lastSeen.epochSeconds
    }

    private suspend fun ensureLastSeenTimestamp() {
        if (lastSeenTimestamp == Instant.DISTANT_PAST.epochSeconds) {
            notificationsApi.getLastSeenTimestamp(userId = userId)?.let {
                updateLastSeenTimestamp(lastSeen = it)
            }
        }
    }

    override suspend fun initialize(): InitializeAction {
        val notificationsCount =
            withContext(dispatcherProvider.io()) { database.notifications().allCount(ownerId = userId) }
        return if (notificationsCount == 0) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Notification>): MediatorResult {
        val timestamp: Long? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                state.firstItemOrNull()?.data?.createdAt
                    ?: withContext(dispatcherProvider.io()) {
                        database.notifications().first(ownerId = userId)?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                state.lastItemOrNull()?.data?.createdAt
                    ?: withContext(dispatcherProvider.io()) {
                        database.notifications().last(ownerId = userId)?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        if (timestamp == null && loadType != LoadType.REFRESH) {
            return MediatorResult.Error(IllegalStateException("Remote key not found."))
        }

        val initialRequestBody = NotificationsRequestBody(
            pubkey = userId,
            userPubkey = userId,
            limit = state.config.pageSize,
        )

        val requestBody = when (loadType) {
            LoadType.REFRESH -> initialRequestBody
            LoadType.PREPEND -> initialRequestBody.copy(
                since = timestamp,
                until = Clock.System.now().epochSeconds,
            )

            LoadType.APPEND -> initialRequestBody.copy(until = timestamp)
        }

        if (lastRequests[loadType] == requestBody) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        val response = try {
            withContext(dispatcherProvider.io()) {
                ensureLastSeenTimestamp()
                notificationsApi.getNotifications(body = requestBody)
            }
        } catch (error: WssException) {
            Napier.w(error) { "Failed to get notifications." }
            return MediatorResult.Error(error)
        }

        lastRequests[loadType] = requestBody

        val userProfileStats = response.primalUserProfileStats.mapNotNullAsProfileStatsPO()
        val notifications = response.primalNotifications.mapNotNullAsNotificationPO()

        withContext(dispatcherProvider.io()) {
            FeedResponse(
                paging = null,
                metadata = response.metadata,
                notes = response.notes,
                articles = emptyList(),
                reposts = emptyList(),
                zaps = emptyList(),
                referencedEvents = response.primalReferencedNotes,
                primalEventStats = response.primalNoteStats,
                primalEventUserStats = emptyList(),
                cdnResources = response.cdnResources,
                primalLinkPreviews = response.primalLinkPreviews,
                primalRelayHints = response.primalRelayHints,
                primalUserNames = response.primalUserNames,
                primalLegendProfiles = response.primalLegendProfiles,
                primalPremiumInfo = response.primalPremiumInfo,
                blossomServers = response.blossomServers,
            ).persistToDatabaseAsTransaction(
                userId = userId,
                database = database,
            )

            database.withTransaction {
                database.profileStats().upsertAll(data = userProfileStats)
                database.notifications().upsertAll(data = notifications.mapWithSeenAtTimestamps())
            }
        }

        return MediatorResult.Success(endOfPaginationReached = false)
    }

    private fun List<NotificationData>.mapWithSeenAtTimestamps(): List<NotificationData> {
        return this.map {
            val seenAt = if (it.createdAt <= lastSeenTimestamp) lastSeenTimestamp else null
            it.copy(seenGloballyAt = seenAt)
        }
    }
}
