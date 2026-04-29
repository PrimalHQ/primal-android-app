package net.primal.android.main.explore.landing

import net.primal.android.core.compose.profile.model.UserProfileItemUi

private const val MAX_RECOMMENDED_USERS = 12

interface ExploreLandingContract {
    data class UiState(
        val recentUsers: List<UserProfileItemUi> = emptyList(),
        val popularUsers: List<UserProfileItemUi> = emptyList(),
        val recentSearches: List<String> = emptyList(),
        val recentSearchesLoading: Boolean = true,
    ) {
        val recommendedUsers: List<UserProfileItemUi> get() =
            (recentUsers + popularUsers).distinctBy { it.profileId }.take(MAX_RECOMMENDED_USERS)
    }
}
