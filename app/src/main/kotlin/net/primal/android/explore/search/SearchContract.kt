package net.primal.android.explore.search

import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.explore.search.ui.SearchScope

interface SearchContract {

    data class UiState(
        val searching: Boolean = false,
        val searchQuery: String = "",
        val searchResults: List<UserProfileItemUi> = emptyList(),
        val recentUsers: List<UserProfileItemUi> = emptyList(),
        val popularUsers: List<UserProfileItemUi> = emptyList(),
    ) {
        val recommendedUsers: List<UserProfileItemUi> get() = (recentUsers + popularUsers).distinctBy { it.profileId }
    }

    sealed class UiEvent {
        data class SearchQueryUpdated(val query: String) : UiEvent()
        data class ProfileSelected(val profileId: String) : UiEvent()
        data object ResetSearchQuery : UiEvent()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onAdvancedSearchClick: (query: String) -> Unit,
        val onProfileClick: (String) -> Unit,
        val onNoteClick: (String) -> Unit,
        val onNaddrClick: (String) -> Unit,
        val onSearchContent: (scope: SearchScope, query: String) -> Unit,
    )
}
