package net.primal.android.messages.api.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import net.primal.android.db.PrimalDatabase
import net.primal.android.messages.api.MessagesApi
import net.primal.android.messages.db.MessageData

@ExperimentalPagingApi
class MessagesRemoteMediator(
    private val userId: String,
    private val messagesApi: MessagesApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, MessageData>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MessageData>
    ): MediatorResult {
        return MediatorResult.Success(endOfPaginationReached = true)
    }

}
