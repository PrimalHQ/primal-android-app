package net.primal.android.settings.muted

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.errors.UiError

interface MutedSettingsContract {
    data class UiState(
        val mutedUsers: List<ProfileDetailsUi> = emptyList(),
        val mutedWords: List<String> = emptyList(),
        val mutedHashtags: List<String> = emptyList(),
        val mutedThreads: List<String> = emptyList(),
        val newMutedHashtag: String = "",
        val newMutedWord: String = "",
        val defaultMuteThreadsFeedSpec: String = "{\"id\":\"muted-threads\",\"kind\":\"notes\"}",
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class UnmuteUser(val pubkey: String) : UiEvent()

        data class MuteWord(val word: String) : UiEvent()
        data class UnmuteWord(val word: String) : UiEvent()

        data class MuteHashtag(val hashtag: String) : UiEvent()
        data class UnmuteHashtag(val hashtag: String) : UiEvent()

        data class UpdateNewMutedHashtag(val hashtag: String) : UiEvent()
        data class UpdateNewMutedWord(val word: String) : UiEvent()

        class DismissError : UiEvent()
    }
}
