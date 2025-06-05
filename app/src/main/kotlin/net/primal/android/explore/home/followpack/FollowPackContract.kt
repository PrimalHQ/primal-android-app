package net.primal.android.explore.home.followpack

import net.primal.android.core.compose.profile.approvals.ProfileApproval
import net.primal.android.core.errors.UiError
import net.primal.android.explore.home.people.model.FollowPackUi

interface FollowPackContract {
    data class UiState(
        val loading: Boolean = true,
        val followPack: FollowPackUi? = null,
        val feedSpec: String? = null,
        val feedDescription: String? = null,
        val shouldApproveProfileAction: ProfileApproval? = null,
        val following: Set<String> = emptySet(),
        val uiError: UiError? = null,
    )

    sealed class UiEvent {
        data object RefreshFollowPack : UiEvent()
        data class FollowAll(val userIds: List<String>, val forceUpdate: Boolean) : UiEvent()
        data class FollowUser(val userId: String, val forceUpdate: Boolean) : UiEvent()
        data class UnfollowUser(val userId: String, val forceUpdate: Boolean) : UiEvent()
        data object DismissConfirmFollowUnfollowAlertDialog : UiEvent()
        data object DismissError : UiEvent()
    }
}
