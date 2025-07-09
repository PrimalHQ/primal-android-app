package net.primal.android.explore.home.followpack

import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.errors.UiError
import net.primal.android.explore.home.people.model.FollowPackUi
import net.primal.android.user.handler.ProfileFollowsHandler

interface FollowPackContract {
    data class UiState(
        val loading: Boolean = true,
        val followPack: FollowPackUi? = null,
        val feedSpec: String? = null,
        val feedDescription: String? = null,
        val shouldApproveFollowsAction: FollowsApproval? = null,
        val following: Set<String> = emptySet(),
        val uiError: UiError? = null,
    )

    sealed class UiEvent {
        data object RefreshFollowPack : UiEvent()
        data class FollowAll(val userIds: List<String>) : UiEvent()
        data class FollowUser(val userId: String) : UiEvent()
        data class UnfollowUser(val userId: String) : UiEvent()
        data class ApproveFollowsActions(val actions: List<ProfileFollowsHandler.Action>) : UiEvent()
        data object DismissConfirmFollowUnfollowAlertDialog : UiEvent()
        data object DismissError : UiEvent()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onShowFeedClick: (feed: String, title: String, description: String) -> Unit,
        val onProfileClick: (String) -> Unit,
    )
}
