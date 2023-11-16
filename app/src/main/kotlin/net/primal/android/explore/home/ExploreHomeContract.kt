package net.primal.android.explore.home

import net.primal.android.user.domain.Badges

interface ExploreHomeContract {
    data class UiState(
        val activeAccountAvatarUrl: String? = null,
        val badges: Badges = Badges(),
        val hashtags: List<List<HashtagUi>> = emptyList(),
    )
}
