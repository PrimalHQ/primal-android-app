package net.primal.android.explore.home.topics

interface ExploreTopicsContract {
    data class UiState(
        val loading: Boolean = true,
        val topics: List<List<TopicUi>> = emptyList(),
    )

    sealed class UiEvent {
        data object RefreshTopics : UiEvent()
    }
}
