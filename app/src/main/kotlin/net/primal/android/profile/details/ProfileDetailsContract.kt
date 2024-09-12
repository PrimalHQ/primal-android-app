package net.primal.android.profile.details

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.profile.domain.ProfileFeedSpec
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
        val userFollowedByProfiles: List<ProfileDetailsUi> = emptyList(),
        val profileStats: ProfileStatsUi? = null,
        val referencedProfilesData: Set<ProfileDetailsUi> = emptySet(),
        val zappingState: ZappingState = ZappingState(),
        val notes: Flow<PagingData<FeedPostUi>>,
        val profileFeedSpec: ProfileFeedSpec = ProfileFeedSpec.AuthoredNotes,
        val confirmBookmarkingNoteId: String? = null,
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
        data class AddUserFeedAction(val name: String, val profileId: String) : UiEvent()
        data class RemoveUserFeedAction(val profileId: String) : UiEvent()
        data class MuteAction(val profileId: String) : UiEvent()
        data class UnmuteAction(val profileId: String) : UiEvent()
        data class ChangeProfileFeed(val profileFeedSpec: ProfileFeedSpec) : UiEvent()
        data object RequestProfileUpdate : UiEvent()
        data class ReportAbuse(
            val reportType: ReportType,
            val profileId: String,
            val noteId: String? = null,
        ) : UiEvent()
        data object DismissError : UiEvent()
        data class BookmarkAction(val noteId: String, val forceUpdate: Boolean = false) : UiEvent()
        data object DismissBookmarkConfirmation : UiEvent()
    }
}
