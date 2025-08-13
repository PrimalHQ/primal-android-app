package net.primal.android.profile.mention

import net.primal.android.core.compose.profile.model.UserProfileItemUi

data class UserTaggingState(
    val userTaggingQuery: String? = null,
    val searchResults: List<UserProfileItemUi> = emptyList(),
    val popularUsers: List<UserProfileItemUi> = emptyList(),
    val recentUsers: List<UserProfileItemUi> = emptyList(),
    val isSearching: Boolean = false,
) {
    val isUserTaggingActive: Boolean get() = userTaggingQuery != null

    val recommendedUsers: List<UserProfileItemUi> get() = recentUsers + popularUsers
}
