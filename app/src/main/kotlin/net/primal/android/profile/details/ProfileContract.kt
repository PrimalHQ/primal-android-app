package net.primal.android.profile.details

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi

interface ProfileContract {
    data class UiState(
        val profileId: String,
        val profileDetails: ProfileDetailsUi? = null,
        val profileStats: ProfileStatsUi? = null,
        val authoredPosts: Flow<PagingData<FeedPostUi>>,
    )

    sealed class UiEvent {

    }

}