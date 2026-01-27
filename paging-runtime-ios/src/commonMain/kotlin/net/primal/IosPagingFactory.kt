package net.primal

import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

object IosPagingFactory {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun <T : Any> createPresenter(pagingFlow: Flow<PagingData<T>>): IosPagingPresenter<T> {
        return IosPagingPresenter(pagingFlow, scope)
    }

    fun <T : Any> createSnapshot(pagingFlow: Flow<PagingData<T>>): IosPagingSnapshot<T> {
        return IosPagingSnapshot(pagingFlow, scope)
    }
}
