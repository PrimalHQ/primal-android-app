package net.primal.android.settings.muted.list

import net.primal.android.settings.muted.list.model.MutedUserUi

interface MutedSettingsContract {
    data class UiState(
        val mutedUsers: List<MutedUserUi> = emptyList(),
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
