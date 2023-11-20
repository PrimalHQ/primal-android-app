package net.primal.android.profile.details

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi

interface ProfileContract {
    data class UiState(
        val profileId: String,
        val isProfileFollowed: Boolean,
        val isProfileMuted: Boolean,
        val isActiveUser: Boolean,
        val isProfileFeedInActiveUserFeeds: Boolean,
        val profileDetails: ProfileDetailsUi? = null,
        val profileStats: ProfileStatsUi? = null,
        val walletConnected: Boolean = false,
        val defaultZapAmount: ULong? = null,
        val zapOptions: List<ULong> = emptyList(),
        val authoredPosts: Flow<PagingData<FeedPostUi>>,
        val error: ProfileError? = null,
    ) {
        sealed class ProfileError {
            data class MissingLightningAddress(val cause: Throwable) : ProfileError()
            data class InvalidZapRequest(val cause: Throwable) : ProfileError()
            data class FailedToPublishZapEvent(val cause: Throwable) : ProfileError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : ProfileError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : ProfileError()
            data class MissingRelaysConfiguration(val cause: Throwable) : ProfileError()
            data class FailedToFollowProfile(val cause: Throwable) : ProfileError()
            data class FailedToUnfollowProfile(val cause: Throwable) : ProfileError()
            data class FailedToAddToFeed(val cause: Throwable) : ProfileError()
            data class FailedToRemoveFeed(val cause: Throwable) : ProfileError()
            data class FailedToMuteProfile(val cause: Throwable) : ProfileError()
            data class FailedToUnmuteProfile(val cause: Throwable) : ProfileError()
        }
    }

    sealed class UiEvent {
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String,
        ) : UiEvent()

        data class ZapAction(
            val postId: String,
            val postAuthorId: String,
            val zapAmount: ULong?,
            val zapDescription: String?,
        ) : UiEvent()

        data class FollowAction(val profileId: String) : UiEvent()
        data class UnfollowAction(val profileId: String) : UiEvent()
        data class AddUserFeedAction(val name: String, val directive: String) : UiEvent()
        data class RemoveUserFeedAction(val directive: String) : UiEvent()
        data class MuteAction(val profileId: String) : UiEvent()
        data class UnmuteAction(val profileId: String) : UiEvent()
    }
}
