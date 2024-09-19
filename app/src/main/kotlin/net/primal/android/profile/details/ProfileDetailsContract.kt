package net.primal.android.profile.details

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
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
        val profileFeedSpecs: List<ProfileFeedSpec> = listOf(
            ProfileFeedSpec.AuthoredNotes,
            ProfileFeedSpec.AuthoredReplies,
            ProfileFeedSpec.AuthoredArticles,
        ),
        val error: ProfileError? = null,
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
    }

    sealed class UiEvent {
        data class FollowAction(val profileId: String) : UiEvent()
        data class UnfollowAction(val profileId: String) : UiEvent()
        data class AddUserFeedAction(val name: String, val profileId: String) : UiEvent()
        data class RemoveUserFeedAction(val profileId: String) : UiEvent()
        data class MuteAction(val profileId: String) : UiEvent()
        data class UnmuteAction(val profileId: String) : UiEvent()
        data object RequestProfileUpdate : UiEvent()
        data class ReportAbuse(val type: ReportType, val profileId: String, val noteId: String? = null) : UiEvent()
        data object DismissError : UiEvent()
    }
}
