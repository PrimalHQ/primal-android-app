package net.primal.android.explore.search

import net.primal.android.explore.search.ui.UserProfileUi

interface SearchContract {

    data class UiState(
        val searching: Boolean = false,
        val searchQuery: String = "",
        val searchResults: List<UserProfileUi> = emptyList(),
        val recommendedUsers: List<UserProfileUi> = emptyList(),
    )

    sealed class UiEvent {
        data class SearchQueryUpdated(val query: String) : UiEvent()
    }
}
