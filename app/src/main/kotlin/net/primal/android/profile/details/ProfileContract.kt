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
        val isProfileFollowed: Boolean,
        val profileDetails: ProfileDetailsUi? = null,
        val profileStats: ProfileStatsUi? = null,
        val walletConnected: Boolean = false,
        val resources: List<MediaResourceUi> = emptyList(),
        val authoredPosts: Flow<PagingData<FeedPostUi>>,
        val error: ProfileError? = null,
    ) {
        sealed class ProfileError {
            data class MissingLightningAddress(val cause: Throwable) : ProfileError()
            data class InvalidZapRequest(val cause: Throwable) : ProfileError()
            data class FailedToPublishZapEvent(val cause: Throwable) : ProfileError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : ProfileError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : ProfileError()
        }
    }

    sealed class UiEvent {
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String
        ) : UiEvent()

        data class ZapAction(
            val postId: String,
            val postAuthorId: String,
            val postAuthorLightningAddress: String?,
            val zapAmount: Int?,
            val zapDescription: String?,
        ) : UiEvent()

        data class FollowAction(val profileId: String) : UiEvent()
        data class UnfollowAction(val profileId: String) : UiEvent()
    }

}
