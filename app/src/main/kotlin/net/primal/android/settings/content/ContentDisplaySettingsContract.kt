package net.primal.android.settings.content

interface ContentDisplaySettingsContract {

    data class UiState(
        val autoPlayVideos: Int = AUTO_PLAY_ALWAYS,
        val showAnimatedAvatars: Boolean = true,
        val focusMode: Boolean = false,
    )

    sealed class UiEvent {
        data class UpdateAutoPlayVideos(val code: Int) : UiEvent()
        data class UpdateShowAnimatedAvatars(val enabled: Boolean) : UiEvent()
        data class UpdateShowFocusMode(val enabled: Boolean) : UiEvent()
    }
}

const val AUTO_PLAY_ALWAYS = 1
const val AUTO_PLAY_ONLY_ON_WIFI = 2
const val AUTO_PLAY_NEVER = 3
