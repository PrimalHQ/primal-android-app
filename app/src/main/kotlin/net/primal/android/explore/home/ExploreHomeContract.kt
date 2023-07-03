package net.primal.android.explore.home

interface ExploreHomeContract {
    data class UiState(
        val activeAccountAvatarUrl: String? = null,
        val hashtags: List<List<HashtagUi>> = emptyList()
    )
}