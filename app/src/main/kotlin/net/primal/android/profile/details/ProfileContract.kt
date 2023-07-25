package net.primal.android.profile.details

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.profile.details.model.ProfileDetailsUi
import net.primal.android.profile.details.model.ProfileStatsUi

interface ProfileContract {
    data class UiState(
        val profileId: String,
        val profileDetails: ProfileDetailsUi? = null,
        val profileStats: ProfileStatsUi? = null,
        val resources: List<MediaResourceUi> = emptyList(),
        val authoredPosts: Flow<PagingData<FeedPostUi>>,
    )

    sealed class UiEvent {
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String
        ) : UiEvent()
    }
}
