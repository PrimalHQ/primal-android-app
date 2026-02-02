package net.primal

import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

object IosPagingFactory {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Option A: Original generic version
    fun <T : Any> createPresenter(pagingFlow: Flow<PagingData<T>>): IosPagingPresenter<T> {
        return IosPagingPresenter(pagingFlow, scope)
    }

    fun <T : Any> createSnapshot(pagingFlow: Flow<PagingData<T>>): IosPagingSnapshot<T> {
        return IosPagingSnapshot(pagingFlow, scope)
    }

    // Option B: Star projection — accepts any PagingData, returns Any
    @Suppress("UNCHECKED_CAST")
    fun createPresenterUntyped(pagingFlow: Flow<PagingData<*>>): IosPagingPresenter<Any> {
        return IosPagingPresenter(pagingFlow as Flow<PagingData<Any>>, scope)
    }

    @Suppress("UNCHECKED_CAST")
    fun createSnapshotUntyped(pagingFlow: Flow<PagingData<*>>): IosPagingSnapshot<Any> {
        return IosPagingSnapshot(pagingFlow as Flow<PagingData<Any>>, scope)
    }

    // Option C: Explicit Any parameter — no generics at all
    fun createPresenterAny(pagingFlow: Flow<PagingData<Any>>): IosPagingPresenter<Any> {
        return IosPagingPresenter(pagingFlow, scope)
    }

    fun createSnapshotAny(pagingFlow: Flow<PagingData<Any>>): IosPagingSnapshot<Any> {
        return IosPagingSnapshot(pagingFlow, scope)
    }
}
