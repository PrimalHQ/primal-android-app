package net.primal.android.explore.home.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.explore.home.topics.ExploreTopicsContract.UiEvent
import net.primal.android.explore.home.topics.ExploreTopicsContract.UiState
import net.primal.android.explore.home.topics.ui.TopicUi
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.explore.ExploreTrendingTopic
import timber.log.Timber

@HiltViewModel
class ExploreTopicsViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchLatestTrendingTopics()
        observeEvents()
        observeTrendingTopics()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RefreshTopics -> fetchLatestTrendingTopics()
                }
            }
        }

    private fun fetchLatestTrendingTopics() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                exploreRepository.fetchTrendingTopics()
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeTrendingTopics() =
        viewModelScope.launch {
            exploreRepository.observeTrendingTopics()
                .map { data -> data.map { it.asTopicUi() } }
                .collect {
                    setState { copy(topics = it.chunked(size = 3)) }
                }
        }

    private fun ExploreTrendingTopic.asTopicUi() = TopicUi(name = this.topic, score = this.score)
}
