package net.primal.android.messages.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.db.PrimalDatabase
import net.primal.android.messages.api.MessagesApi
import net.primal.android.messages.api.model.MessagesRequestBody
import net.primal.android.messages.db.DirectMessage
import net.primal.networking.sockets.errors.WssException
import timber.log.Timber

@ExperimentalPagingApi
class MessagesRemoteMediator(
    private val userId: String,
    private val participantId: String,
    private val database: PrimalDatabase,
    private val messagesApi: MessagesApi,
    private val messagesProcessor: MessagesProcessor,
) : RemoteMediator<Int, DirectMessage>() {

    private val lastRequests: MutableMap<LoadType, MessagesRequestBody> = mutableMapOf()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, DirectMessage>): MediatorResult {
        val timestamp: Long? = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> {
                state.firstItemOrNull()?.data?.createdAt
                    ?: withContext(Dispatchers.IO) {
                        database.messages().firstByOwnerId(ownerId = userId)?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                state.lastItemOrNull()?.data?.createdAt
                    ?: withContext(Dispatchers.IO) {
                        database.messages().lastByOwnerId(ownerId = userId)?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        if (timestamp == null && loadType != LoadType.REFRESH) {
            return MediatorResult.Error(IllegalStateException("Remote key not found."))
        }

        val initialRequestBody = MessagesRequestBody(
            userId = userId,
            participantId = participantId,
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
                messagesApi.getMessages(body = requestBody)
            }
        } catch (error: WssException) {
            Timber.w(error)
            return MediatorResult.Error(error)
        }

        lastRequests[loadType] = requestBody

        withContext(Dispatchers.IO) {
            messagesProcessor.processMessageEventsAndSave(
                userId = userId,
                messages = response.messages,
                profileMetadata = response.profileMetadata,
                mediaResources = response.cdnResources,
                primalUserNames = response.primalUserNames,
                primalPremiumInfo = response.primalPremiumInfo,
                primalLegendProfiles = response.primalLegendProfiles,
                blossomServerEvents = response.blossomServers,
            )
        }

        return MediatorResult.Success(endOfPaginationReached = false)
    }
}
