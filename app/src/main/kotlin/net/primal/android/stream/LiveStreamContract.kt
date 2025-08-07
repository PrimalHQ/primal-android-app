package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.stream.ui.StreamChatItem
import net.primal.android.user.handler.ProfileFollowsHandler

interface LiveStreamContract {
    data class UiState(
        val loading: Boolean = true,
        val profileId: String? = null,
        val isFollowed: Boolean = false,
        val streamInfo: StreamInfoUi? = null,
        val authorProfile: ProfileDetailsUi? = null,
        val profileStats: ProfileStatsUi? = null,
        val playerState: PlayerState = PlayerState(),
        val comment: TextFieldValue = TextFieldValue(),
        val shouldApproveProfileAction: FollowsApproval? = null,
        val zaps: List<EventZapUiModel> = emptyList(),
        val chatItems: List<StreamChatItem> = emptyList(),
        val zappingState: ZappingState = ZappingState(),
        val sendingMessage: Boolean = false,
        val error: UiError? = null,
    )

    data class PlayerState(
        val isPlaying: Boolean = false,
        val isBuffering: Boolean = false,
        val atLiveEdge: Boolean = false,
        val isSeeking: Boolean = false,
        val currentTime: Long = 0L,
        val totalDuration: Long = 0L,
        val isLive: Boolean = false,
    )

    data class StreamInfoUi(
        val atag: String,
        val eventId: String,
        val title: String,
        val streamUrl: String,
        val viewers: Int,
        val startedAt: Long?,
        val description: String?,
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
        data class ZapStream(val zapAmount: ULong? = null, val zapDescription: String? = null) : UiEvent()
        data class OnCommentValueChanged(val value: TextFieldValue) : UiEvent()
        data class SendMessage(val text: String) : UiEvent()
    }

    sealed class SideEffect
}
