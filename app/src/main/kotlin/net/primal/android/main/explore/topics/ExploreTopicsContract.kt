package net.primal.android.main.explore.topics

import net.primal.android.main.explore.topics.ui.TopicUi

interface ExploreTopicsContract {
    data class UiState(
        val loading: Boolean = true,
        val topics: List<List<TopicUi>> = emptyList(),
    )

    sealed class UiEvent {
        data object RefreshTopics : UiEvent()
    }
}
