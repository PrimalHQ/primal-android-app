package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.user.handler.ProfileFollowsHandler

interface LiveStreamContract {
    data class UiState(
        val loading: Boolean = true,
        val profileId: String? = null,
        val isFollowed: Boolean = false,
        val streamInfo: StreamInfoUi? = null,
        val isLive: Boolean = false,
        val isPlaying: Boolean = false,
        val isBuffering: Boolean = false,
        val atLiveEdge: Boolean = false,
        val isSeeking: Boolean = false,
        val currentTime: Long = 0L,
        val totalDuration: Long = 0L,
        val comment: TextFieldValue = TextFieldValue(),
        val profileStats: ProfileStatsUi? = null,
        val shouldApproveProfileAction: FollowsApproval? = null,
        val error: UiError? = null,
    )

    data class StreamInfoUi(
        val title: String,
        val streamUrl: String,
        val authorProfile: ProfileDetailsUi,
        val viewers: Int,
        val startedAt: Long?,
    )

    sealed class UiEvent {
        data class OnPlayerStateUpdate(
            val isPlaying: Boolean? = null,
            val isBuffering: Boolean? = null,
            val atLiveEdge: Boolean? = null,
            val currentTime: Long? = null,
            val totalDuration: Long? = null,
        ) : UiEvent()

        data object OnSeekStarted : UiEvent()
        data class OnSeek(val positionMs: Long) : UiEvent()
        data class FollowAction(val profileId: String) : UiEvent()
        data class UnfollowAction(val profileId: String) : UiEvent()
        data object DismissError : UiEvent()
        data object DismissConfirmFollowUnfollowAlertDialog : UiEvent()
        data class ApproveFollowsActions(val actions: List<ProfileFollowsHandler.Action>) : UiEvent()
    }

    sealed class SideEffect
}
