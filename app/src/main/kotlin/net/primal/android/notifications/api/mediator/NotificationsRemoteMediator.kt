package net.primal.android.notifications.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.mapNotNullAsNotificationPO
import net.primal.android.nostr.ext.mapNotNullAsProfileStatsPO
import net.primal.android.notes.api.model.FeedResponse
import net.primal.android.notes.repository.persistToDatabaseAsTransaction
import net.primal.android.notifications.api.NotificationsApi
import net.primal.android.notifications.api.model.NotificationsRequestBody
import net.primal.android.notifications.db.Notification
import net.primal.android.notifications.db.NotificationData
import timber.log.Timber

@ExperimentalPagingApi
class NotificationsRemoteMediator(
    private val userId: String,
    private val notificationsApi: NotificationsApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, Notification>() {

    private var lastSeenTimestamp: Long = Instant.EPOCH.epochSecond

    private val lastRequests: MutableMap<LoadType, NotificationsRequestBody> = mutableMapOf()

    fun updateLastSeenTimestamp(lastSeen: Instant) {
        lastSeenTimestamp = lastSeen.epochSecond
    }

    private suspend fun ensureLastSeenTimestamp() {
        if (lastSeenTimestamp == Instant.EPOCH.epochSecond) {
            notificationsApi.getLastSeenTimestamp(userId = userId)?.let {
                updateLastSeenTimestamp(lastSeen = it)
            }
        }
    }

    override suspend fun initialize(): InitializeAction {
        val notificationsCount = withContext(Dispatchers.IO) { database.notifications().allCount(ownerId = userId) }
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
                    ?: withContext(Dispatchers.IO) {
                        database.notifications().first(ownerId = userId)?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                state.lastItemOrNull()?.data?.createdAt
                    ?: withContext(Dispatchers.IO) {
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
                until = Instant.now().epochSecond,
            )

            LoadType.APPEND -> initialRequestBody.copy(until = timestamp)
        }

        if (lastRequests[loadType] == requestBody) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        val response = try {
            withContext(Dispatchers.IO) {
                ensureLastSeenTimestamp()
                notificationsApi.getNotifications(body = requestBody)
            }
        } catch (error: WssException) {
            Timber.w(error)
            return MediatorResult.Error(error)
        }

        lastRequests[loadType] = requestBody

        val userProfileStats = response.primalUserProfileStats.mapNotNullAsProfileStatsPO()
        val notifications = response.primalNotifications.mapNotNullAsNotificationPO()

        withContext(Dispatchers.IO) {
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
