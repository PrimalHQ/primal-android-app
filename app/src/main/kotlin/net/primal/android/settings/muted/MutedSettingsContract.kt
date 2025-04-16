package net.primal.android.settings.muted

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface MutedSettingsContract {
    data class UiState(
        val mutedUsers: List<ProfileDetailsUi> = emptyList(),
        val mutedWords: List<String> = listOf<String>("sample1", "sample2", "sample3"),
        val mutedHashtags: List<String> = listOf<String>("#example1", "#example2", "#example3"),
        val mutedThreads: List<String> = emptyList(),
        val error: MutedSettingsError? = null,
    ) {
        sealed class MutedSettingsError {
            data class FailedToUnmuteUserError(val error: Throwable) : MutedSettingsError()
        }
    }

    sealed class UiEvent {
        data class UnmuteUser(val pubkey: String) : UiEvent()

        data class MuteWord(val word: String) : UiEvent()
        data class UnmuteWord(val word: String) : UiEvent()

        data class MuteHashtag(val hashtag: String) : UiEvent()
        data class UnmuteHashtag(val hashtag: String) : UiEvent()

        data class MuteThread(val threadId: String) : UiEvent()
        data class UnmuteThread(val threadId: String) : UiEvent()
    }
}
