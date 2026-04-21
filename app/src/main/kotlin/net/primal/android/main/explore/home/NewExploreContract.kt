package net.primal.android.main.explore.home

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.main.explore.people.model.FollowPackUi

interface NewExploreContract {

    data class UiState(
        val popularUsers: List<UserProfileItemUi> = emptyList(),
        val followPacks: Flow<PagingData<FollowPackUi>> = emptyFlow(),
        val feeds: List<DvmFeedUi> = emptyList(),
    )
}
