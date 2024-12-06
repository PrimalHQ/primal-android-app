package net.primal.android.profile.details

import net.primal.android.core.compose.profile.approvals.ProfileApproval
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.profile.domain.ProfileFeedSpec
import net.primal.android.profile.report.ReportType

interface ProfileDetailsContract {
    data class UiState(
        val profileId: String,
        val isActiveUser: Boolean,
        val activeUserHasPremiumMembership: Boolean = false,
        val isProfileFollowed: Boolean = false,
        val isProfileFollowingMe: Boolean = false,
        val isProfileMuted: Boolean = false,
        val isProfileFeedInActiveUserFeeds: Boolean = false,
        val profileDetails: ProfileDetailsUi? = null,
        val userFollowedByProfiles: List<ProfileDetailsUi> = emptyList(),
        val profileStats: ProfileStatsUi? = null,
        val referencedProfilesData: Set<ProfileDetailsUi> = emptySet(),
        val profileFeedSpecs: List<ProfileFeedSpec> = listOf(
            ProfileFeedSpec.AuthoredNotes,
            ProfileFeedSpec.AuthoredReplies,
            ProfileFeedSpec.AuthoredArticles,
            ProfileFeedSpec.AuthoredMedia,
        ),
        val error: ProfileError? = null,
        val shouldApproveProfileAction: ProfileApproval? = null,
        val zapError: UiError? = null,
        val zappingState: ZappingState = ZappingState(),
    ) {
        sealed class ProfileError {
            data class MissingRelaysConfiguration(val cause: Throwable) : ProfileError()
            data class FailedToFollowProfile(val cause: Throwable) : ProfileError()
            data class FailedToUnfollowProfile(val cause: Throwable) : ProfileError()
            data class FailedToAddToFeed(val cause: Throwable) : ProfileError()
            data class FailedToRemoveFeed(val cause: Throwable) : ProfileError()
            data class FailedToMuteProfile(val cause: Throwable) : ProfileError()
            data class FailedToUnmuteProfile(val cause: Throwable) : ProfileError()
        }
    }

    sealed class SideEffect {
        data object ProfileUpdateFinished : SideEffect()
        data object ProfileFeedAdded : SideEffect()
        data object ProfileFeedRemoved : SideEffect()
        data object ProfileZapSent : SideEffect()
    }

    sealed class UiEvent {
        data class AddProfileFeedAction(
            val profileId: String,
            val feedTitle: String,
            val feedDescription: String,
        ) : UiEvent()
        data class ZapProfile(
            val profileId: String,
            val profileLnUrlDecoded: String?,
            val zapDescription: String? = null,
            val zapAmount: ULong? = null,
        ) : UiEvent()
        data class FollowAction(val profileId: String, val forceUpdate: Boolean) : UiEvent()
        data class UnfollowAction(val profileId: String, val forceUpdate: Boolean) : UiEvent()
        data class RemoveProfileFeedAction(val profileId: String) : UiEvent()
        data class MuteAction(val profileId: String) : UiEvent()
        data class UnmuteAction(val profileId: String) : UiEvent()
        data object RequestProfileUpdate : UiEvent()
        data class ReportAbuse(val type: ReportType, val profileId: String, val noteId: String? = null) : UiEvent()
        data object DismissError : UiEvent()
        data object DismissZapError : UiEvent()
        data object DismissConfirmFollowUnfollowAlertDialog : UiEvent()
    }
}
