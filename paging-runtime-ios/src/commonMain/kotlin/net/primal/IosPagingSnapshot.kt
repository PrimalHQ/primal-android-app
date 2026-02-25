package net.primal

import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.primal.core.utils.coroutines.createDispatcherProvider

class IosPagingSnapshot<T : Any> internal constructor(
    pagingFlow: Flow<PagingData<T>>,
    scope: CoroutineScope,
) {

    private val dispatcher = createDispatcherProvider().io()

    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    private val presenter = object : PagingDataPresenter<T>(
        mainContext = dispatcher,
        cachedPagingData = null,
    ) {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) {
            rebuildSnapshot()
        }
    }

    val loadStates: StateFlow<CombinedLoadStates?> = presenter.loadStateFlow

    private val job: Job = scope.launch(dispatcher) {
        pagingFlow
            .cachedIn(this)
            .collect { presenter.collectFrom(it) }
    }

    private fun rebuildSnapshot() {
        val size = presenter.size
        val snapshot = ArrayList<T>(size)
        for (i in 0 until size) {
            val item = presenter.peek(i)
            if (item != null) {
                snapshot.add(item)
            }
        }
        _items.value = snapshot
    }

    fun access(index: Int) {
        presenter[index]
    }

    fun accessLast() {
        val lastIndex = presenter.size - 1
        if (lastIndex >= 0) {
            presenter[lastIndex]
        }
    }

    fun retry() = presenter.retry()

    fun refresh() = presenter.refresh()

    fun dispose() {
        job.cancel()
    }
}
