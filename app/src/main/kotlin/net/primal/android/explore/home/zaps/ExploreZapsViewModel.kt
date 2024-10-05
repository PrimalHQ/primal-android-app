package net.primal.android.explore.home.zaps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.explore.home.zaps.ExploreZapsContract.*
import net.primal.android.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class ExploreZapsViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchExploreZaps()
        observeEvents()
    }

    private fun fetchExploreZaps() =
        viewModelScope.launch {
            try {
                setState { copy(loading = true) }
                val zaps = exploreRepository.fetchTrendingZaps(
                    userId = activeAccountStore.activeUserId(),
                )
                setState { copy(zaps = zaps) }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RefreshZaps -> fetchExploreZaps()
                }
            }
        }

}
