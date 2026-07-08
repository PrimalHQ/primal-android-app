package net.primal.android.main.explore.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import net.primal.android.main.explore.people.ExplorePeopleContract.UiState
import net.primal.android.main.explore.people.model.asFollowPackUi
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.explore.ExploreRepository

@HiltViewModel
class ExplorePeopleViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState(followPacks = buildFollowPacksPager()))
    val state = _state.asStateFlow()

    private fun buildFollowPacksPager() =
        exploreRepository.getFollowLists()
            .map { it.map { it.asFollowPackUi() } }
            .cachedIn(viewModelScope + dispatcherProvider.io())
}
