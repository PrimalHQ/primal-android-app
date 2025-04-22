package net.primal.android.explore.home.people

import net.primal.android.core.compose.profile.approvals.ProfileApproval
import net.primal.android.core.errors.UiError
import net.primal.domain.explore.ExplorePeopleData

interface ExplorePeopleContract {

    data class UiState(
        val loading: Boolean = true,
        val people: List<ExplorePeopleData> = emptyList(),
        val userFollowing: Set<String> = emptySet(),
        val error: UiError? = null,
        val shouldApproveProfileAction: ProfileApproval? = null,
    )

    sealed class UiEvent {
        data class FollowUser(val userId: String, val forceUpdate: Boolean) : UiEvent()
        data class UnfollowUser(val userId: String, val forceUpdate: Boolean) : UiEvent()
        data object DismissConfirmFollowUnfollowAlertDialog : UiEvent()
        data object RefreshPeople : UiEvent()
        data object DismissError : UiEvent()
    }
}
