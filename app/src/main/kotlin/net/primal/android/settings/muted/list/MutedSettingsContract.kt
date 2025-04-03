package net.primal.android.settings.muted.list

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface MutedSettingsContract {
    data class UiState(
        val mutedUsers: List<ProfileDetailsUi> = emptyList(),
        val error: MutedSettingsError? = null,
    ) {
        sealed class MutedSettingsError {
            data class FailedToUnmuteUserError(val error: Throwable) : MutedSettingsError()
        }
    }

    sealed class UiEvent {
        data class UnmuteEvent(val pubkey: String) : UiEvent()
    }
}
