package net.primal.android.explore.home.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import net.primal.android.explore.home.people.ExplorePeopleContract.UiState
import net.primal.android.explore.home.people.model.asFollowPackUi
import net.primal.domain.explore.ExploreRepository

@HiltViewModel
class ExplorePeopleViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState(followPacks = buildFollowPacksPager()))
    val state = _state.asStateFlow()

    private fun buildFollowPacksPager() =
        exploreRepository.getFollowLists()
            .map { it.map { it.asFollowPackUi() } }
            .cachedIn(viewModelScope)
}
