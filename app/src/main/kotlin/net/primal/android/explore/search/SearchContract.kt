package net.primal.android.explore.search

import net.primal.android.core.compose.profile.model.UserProfileItemUi

interface SearchContract {

    data class UiState(
        val searching: Boolean = false,
        val searchQuery: String = "",
        val searchResults: List<UserProfileItemUi> = emptyList(),
        val recommendedUsers: List<UserProfileItemUi> = emptyList(),
    )

    sealed class UiEvent {
        data class SearchQueryUpdated(val query: String) : UiEvent()
    }
}
