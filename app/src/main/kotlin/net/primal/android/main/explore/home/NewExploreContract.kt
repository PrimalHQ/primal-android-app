package net.primal.android.main.explore.home

import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.main.explore.people.model.FollowPackUi

private const val MAX_RECOMMENDED_USERS = 12

interface NewExploreContract {

    data class UiState(
        val recentUsers: List<UserProfileItemUi> = emptyList(),
        val popularUsers: List<UserProfileItemUi> = emptyList(),
        val followPacks: List<FollowPackUi> = emptyList(),
        val feeds: List<DvmFeedUi> = emptyList(),
    ) {
        val recommendedUsers: List<UserProfileItemUi> get() =
            (recentUsers + popularUsers).distinctBy { it.profileId }.take(MAX_RECOMMENDED_USERS)
    }
}
