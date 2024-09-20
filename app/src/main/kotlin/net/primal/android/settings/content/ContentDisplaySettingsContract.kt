package net.primal.android.settings.content

import net.primal.android.user.domain.ContentDisplaySettings

interface ContentDisplaySettingsContract {

    data class UiState(
        val autoPlayVideos: Int = ContentDisplaySettings.AUTO_PLAY_VIDEO_NEVER,
        val showAnimatedAvatars: Boolean = false,
        val focusMode: Boolean = true,
        val tweetMode: Boolean = true,
    )

    sealed class UiEvent {
        data class UpdateAutoPlayVideos(val code: Int) : UiEvent()
        data class UpdateShowAnimatedAvatars(val enabled: Boolean) : UiEvent()
        data class UpdateShowFocusMode(val enabled: Boolean) : UiEvent()
        data class UpdateEnableTweetMode(val enabled: Boolean) : UiEvent()
    }
}
