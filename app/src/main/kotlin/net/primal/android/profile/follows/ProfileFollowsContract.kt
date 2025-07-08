package net.primal.android.profile.follows

import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.errors.UiError
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.user.handler.ProfileFollowsHandler

interface ProfileFollowsContract {
    data class UiState(
        val userId: String,
        val followsType: ProfileFollowsType,
        val profileName: String? = null,
        val loading: Boolean = true,
        val userFollowing: Set<String> = emptySet(),
        val uiError: UiError? = null,
        val users: List<UserProfileItemUi> = emptyList(),
        val shouldApproveProfileAction: FollowsApproval? = null,
    )

    sealed class UiEvent {
        data class FollowProfile(val profileId: String) : UiEvent()
        data class UnfollowProfile(val profileId: String) : UiEvent()
        data class ApproveFollowsActions(val actions: List<ProfileFollowsHandler.Action>) : UiEvent()
        data object DismissConfirmFollowUnfollowAlertDialog : UiEvent()
        data object DismissError : UiEvent()
        data object ReloadData : UiEvent()
    }

    data class ScreenCallbacks(
        val onProfileClick: (String) -> Unit,
        val onClose: () -> Unit,
    )
}
