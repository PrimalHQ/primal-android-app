package net.primal.android.note.reactions.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.note.api.NoteApi
import net.primal.android.note.api.model.NoteZapsRequestBody
import net.primal.android.note.db.NoteZap
import net.primal.android.note.repository.persistToDatabaseAsTransaction

@ExperimentalPagingApi
class NoteZapsMediator(
    private val noteId: String,
    private val userId: String,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val noteApi: NoteApi,
    private val database: PrimalDatabase,
) : RemoteMediator<Int, NoteZap>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, NoteZap>): MediatorResult {
        withContext(dispatcherProvider.io()) {
            val response = noteApi.getNoteZaps(NoteZapsRequestBody(noteId = noteId, userId = userId, limit = 100))
            response.persistToDatabaseAsTransaction(database)
        }
        return MediatorResult.Success(endOfPaginationReached = true)
    }
}
