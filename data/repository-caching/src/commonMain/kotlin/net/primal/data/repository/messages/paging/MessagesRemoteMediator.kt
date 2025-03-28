package net.primal.data.repository.messages.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.dao.messages.DirectMessage
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.messages.MessagesApi
import net.primal.data.remote.api.messages.model.MessagesRequestBody
import net.primal.data.repository.messages.processors.MessagesProcessor

@ExperimentalPagingApi
internal class MessagesRemoteMediator(
    private val userId: String,
    private val participantId: String,
    private val dispatcherProvider: DispatcherProvider,
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
                    ?: withContext(dispatcherProvider.io()) {
                        database.messages().firstByOwnerId(ownerId = userId)?.createdAt
                    }
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                state.lastItemOrNull()?.data?.createdAt
                    ?: withContext(dispatcherProvider.io()) {
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
                until = Clock.System.now().epochSeconds,
            )

            LoadType.APPEND -> initialRequestBody.copy(until = timestamp)
        }

        if (lastRequests[loadType] == requestBody) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        val response = try {
            withContext(dispatcherProvider.io()) {
                messagesApi.getMessages(body = requestBody)
            }
        } catch (error: WssException) {
            Napier.w(error) { "Failed to get remote messages." }
            return MediatorResult.Error(error)
        }

        lastRequests[loadType] = requestBody

        withContext(dispatcherProvider.io()) {
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
