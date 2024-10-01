package net.primal.android.explore.home.topics

import net.primal.android.explore.home.TopicUi

interface ExploreTopicsContract {
    data class UiState(
        val loading: Boolean = true,
        val topics: List<List<TopicUi>> = emptyList(),
    )

    sealed class UiEvent {
        data object RefreshTopics : UiEvent()
    }
}
