package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.profile.mention.UserTaggingState
import net.primal.android.stream.ui.ActiveBottomSheet
import net.primal.android.stream.ui.StreamChatItem
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.ReactionType
import net.primal.domain.nostr.ReportType
import net.primal.domain.streams.StreamContentModerationMode
import net.primal.domain.wallet.DraftTx
import net.primal.domain.zaps.ZappingState

interface LiveStreamContract {
    data class UiState(
        val naddr: Naddr? = null,
        val streamInfoLoading: Boolean = true,
        val chatLoading: Boolean = true,
        val activeUserId: String? = null,
        val streamInfo: StreamInfoUi? = null,
        val playerState: PlayerState = PlayerState(),
        val comment: TextFieldValue = TextFieldValue(),
        val shouldApproveProfileAction: FollowsApproval? = null,
        val contentModerationMode: StreamContentModerationMode = StreamContentModerationMode.Moderated,
        val zaps: List<EventZapUiModel> = emptyList(),
        val chatItems: List<StreamChatItem> = emptyList(),
        val zappingState: ZappingState = ZappingState(),
        val sendingMessage: Boolean = false,
        val taggedUsers: List<NoteTaggedUser> = emptyList(),
        val userTaggingState: UserTaggingState = UserTaggingState(),
        val error: UiError? = null,
        val mainHostStreamsMuted: Boolean = false,
        val activeUserFollowedProfiles: Set<String> = emptySet(),
        val activeUserMutedProfiles: Set<String> = emptySet(),
        val profileIdToFollowerCount: Map<String, Int> = emptyMap(),
        val liveProfiles: Set<String> = emptySet(),
        val isStreamUnavailable: Boolean = false,
        val activeBottomSheet: ActiveBottomSheet = ActiveBottomSheet.None,
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
        val isVideoFinished: Boolean = false,
    ) {
        val isLoading: Boolean get() = isBuffering && !isPlaying
    }

    data class StreamInfoUi(
        val atag: String,
        val eventId: String,
        val title: String,
        val streamUrl: String,
        val viewers: Int,
        val image: String?,
        val startedAt: Long?,
        val description: String?,
        val rawNostrEventJson: String,
        val mainHostId: String,
        val mainHostProfile: ProfileDetailsUi? = null,
        val mainHostProfileStats: ProfileStatsUi? = null,
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
        data object ToggleMute : UiEvent()
        data class FollowAction(val profileId: String) : UiEvent()
        data class UnfollowAction(val profileId: String) : UiEvent()
        data object DismissError : UiEvent()
        data object DismissConfirmFollowUnfollowAlertDialog : UiEvent()
        data class ApproveFollowsActions(val actions: List<ProfileFollowsHandler.Action>) : UiEvent()
        data class ZapStream(val zapAmount: ULong? = null, val zapDescription: String? = null) : UiEvent()
        data class ChangeContentModeration(val moderationMode: StreamContentModerationMode) : UiEvent()
        data class ChangeStreamMuted(val isMuted: Boolean) : UiEvent()
        data class OnCommentValueChanged(val value: TextFieldValue) : UiEvent()
        data class SendMessage(val text: String) : UiEvent()
        data class MuteAction(val profileId: String) : UiEvent()
        data class UnmuteAction(val profileId: String) : UiEvent()
        data class ReportAbuse(val reportType: ReportType) : UiEvent()
        data object RequestDeleteStream : UiEvent()
        data class SearchUsers(val query: String) : UiEvent()
        data class ToggleSearchUsers(val enabled: Boolean) : UiEvent()
        data class TagUser(val taggedUser: NoteTaggedUser) : UiEvent()
        data object AppendUserTagAtSign : UiEvent()
        data class ReportMessage(
            val reportType: ReportType,
            val messageId: String,
            val authorId: String,
        ) : UiEvent()

        data object OnVideoUnavailable : UiEvent()
        data object OnVideoEnded : UiEvent()
        data class ChangeActiveBottomSheet(val sheet: ActiveBottomSheet) : UiEvent()
    }

    sealed class SideEffect {
        data object StreamDeleted : SideEffect()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onGoToWallet: () -> Unit,
        val onEditProfileClick: () -> Unit,
        val onMessageClick: (profileId: String) -> Unit,
        val onDrawerQrCodeClick: (profileId: String) -> Unit,
        val onQuoteStreamClick: (naddr: String) -> Unit,
        val onProfileClick: (profileId: String) -> Unit,
        val onHashtagClick: (hashtag: String) -> Unit,
        val onEventReactionsClick: (eventId: String, initialTab: ReactionType, articleATag: String?) -> Unit,
        val onSendWalletTx: (DraftTx) -> Unit,
        val onNostrUriClick: (uri: String) -> Unit,
    )
}
