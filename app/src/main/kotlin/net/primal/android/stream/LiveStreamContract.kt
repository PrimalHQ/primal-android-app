package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface LiveStreamContract {
    data class UiState(
        val loading: Boolean = true,
        val streamInfo: StreamInfoUi? = null,
        val isLive: Boolean = false,
        val isPlaying: Boolean = false,
        val isBuffering: Boolean = false,
        val atLiveEdge: Boolean = false,
        val isSeeking: Boolean = false,
        val currentTime: Long = 0L,
        val totalDuration: Long = 0L,
        val comment: TextFieldValue = TextFieldValue(),
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
    }

    sealed class SideEffect
}
