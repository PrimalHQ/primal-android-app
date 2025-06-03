package net.primal

import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

object IosPagingUtils {

    private object SharedMainScope : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun <T : Any> buildIosPagingPresenter(pagingData: Flow<PagingData<T>>): IosPagingPresenter<T> {
        return IosPagingPresenter(pagingData, SharedMainScope)
    }
}
