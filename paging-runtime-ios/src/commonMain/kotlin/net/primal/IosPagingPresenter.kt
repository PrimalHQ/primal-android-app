package net.primal

import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import net.primal.core.utils.coroutines.DispatcherProviderFactory

class IosPagingPresenter<T : Any>(
    pagingFlow: Flow<PagingData<T>>,
    scope: CoroutineScope,
    dispatcher: CoroutineDispatcher = DispatcherProviderFactory.create().io(),
) {

    private val presenter = object : PagingDataPresenter<T>(
        mainContext = dispatcher,
        cachedPagingData = null,
    ) {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) {
            _invalidations.tryEmit(Unit)
        }
    }

    private val _invalidations = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 64,
    )

    val invalidations: SharedFlow<Unit> = _invalidations.asSharedFlow()

    val loadStates: StateFlow<CombinedLoadStates?> = presenter.loadStateFlow

    init {
        scope.launch(dispatcher) {
            pagingFlow
                .cachedIn(this)
                .collect { presenter.collectFrom(it) }
        }
    }

    fun itemCount(): Int = presenter.size

    fun item(index: Int): T? = presenter[index]

    fun access(index: Int) = presenter[index]

    fun peek(index: Int): T? = presenter.peek(index)

    fun retry() = presenter.retry()

    fun refresh() = presenter.refresh()
}
