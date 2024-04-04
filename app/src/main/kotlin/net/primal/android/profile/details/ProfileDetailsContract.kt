package net.primal.android.profile.details

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.profile.domain.ProfileFeedDirective
import net.primal.android.profile.report.ReportType

interface ProfileDetailsContract {
    data class UiState(
        val profileId: String,
        val isActiveUser: Boolean,
        val isProfileFollowed: Boolean = false,
        val isProfileFollowingMe: Boolean = false,
        val isProfileMuted: Boolean = false,
        val isProfileFeedInActiveUserFeeds: Boolean = false,
        val profileDetails: ProfileDetailsUi? = null,
        val profileStats: ProfileStatsUi? = null,
        val referencedProfilesData: Set<ProfileDetailsUi> = emptySet(),
        val zappingState: ZappingState = ZappingState(),
        val notes: Flow<PagingData<FeedPostUi>>,
        val profileDirective: ProfileFeedDirective = ProfileFeedDirective.AuthoredNotes,
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
        data class ChangeProfileFeed(val profileDirective: ProfileFeedDirective) : UiEvent()
        data object RequestProfileUpdate : UiEvent()
        data class ReportAbuse(
            val reportType: ReportType,
            val profileId: String,
            val noteId: String? = null,
        ) : UiEvent()
        data class BookmarkAction(val noteId: String) : UiEvent()
        data object DismissError : UiEvent()
    }
}
