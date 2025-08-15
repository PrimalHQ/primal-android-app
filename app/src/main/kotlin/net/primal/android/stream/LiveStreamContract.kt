package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.profile.mention.UserTaggingState
import net.primal.android.stream.ui.StreamChatItem
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.ReportType
import net.primal.domain.zaps.ZappingState

interface LiveStreamContract {
    data class UiState(
        val loading: Boolean = true,
        val naddr: Naddr? = null,
        val activeUserId: String? = null,
        val isBookmarked: Boolean = false,
        val streamInfo: StreamInfoUi? = null,
        val playerState: PlayerState = PlayerState(),
        val comment: TextFieldValue = TextFieldValue(),
        val shouldApproveProfileAction: FollowsApproval? = null,
        val shouldApproveBookmark: Boolean = false,
        val zaps: List<EventZapUiModel> = emptyList(),
        val chatItems: List<StreamChatItem> = emptyList(),
        val zappingState: ZappingState = ZappingState(),
        val sendingMessage: Boolean = false,
        val error: UiError? = null,
        val taggedUsers: List<NoteTaggedUser> = emptyList(),
        val userTaggingState: UserTaggingState = UserTaggingState(),
    )

    data class PlayerState(
        val isPlaying: Boolean = false,
        val isMuted: Boolean = false,
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
        val rawNostrEventJson: String,
        val authorId: String,
        val authorProfile: ProfileDetailsUi? = null,
        val authorProfileStats: ProfileStatsUi? = null,
        val isFollowedByActiveUser: Boolean = false,
        val isMutedByActiveUser: Boolean = false,
    )

    sealed class UiEvent {
        data class StartStream(val naddr: String) : UiEvent()
        data class OnPlayerStateUpdate(
            val isPlaying: Boolean? = null,
            val isBuffering: Boolean? = null,
            val atLiveEdge: Boolean? = null,
            val currentTime: Long? = null,
            val totalDuration: Long? = null,
        ) : UiEvent()

        data object OnSeekStarted : UiEvent()
        data class OnSeek(val positionMs: Long) : UiEvent()
        data object ToggleMute : UiEvent()
        data class FollowAction(val profileId: String) : UiEvent()
        data class UnfollowAction(val profileId: String) : UiEvent()
        data object DismissError : UiEvent()
        data object DismissConfirmFollowUnfollowAlertDialog : UiEvent()
        data object DismissBookmarkConfirmation : UiEvent()
        data class ApproveFollowsActions(val actions: List<ProfileFollowsHandler.Action>) : UiEvent()
        data class ZapStream(val zapAmount: ULong? = null, val zapDescription: String? = null) : UiEvent()
        data class OnCommentValueChanged(val value: TextFieldValue) : UiEvent()
        data class SendMessage(val text: String) : UiEvent()
        data class MuteAction(val profileId: String) : UiEvent()
        data class UnmuteAction(val profileId: String) : UiEvent()
        data class ReportAbuse(val reportType: ReportType) : UiEvent()
        data object RequestDeleteStream : UiEvent()
        data class BookmarkStream(val forceUpdate: Boolean = false) : UiEvent()
        data class QuoteStream(val naddr: String) : UiEvent()
        data class SearchUsers(val query: String) : UiEvent()
        data class ToggleSearchUsers(val enabled: Boolean) : UiEvent()
        data class TagUser(val taggedUser: NoteTaggedUser) : UiEvent()
        data object AppendUserTagAtSign : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToQuote(val naddr: String) : SideEffect()
        data object StreamDeleted : SideEffect()
    }
}
