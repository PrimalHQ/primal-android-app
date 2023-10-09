package net.primal.android.settings.muted

import net.primal.android.settings.muted.model.MutedUser

interface MutedSettingsContract {
    data class UiState(
        val mutelist: List<MutedUser> = emptyList(),
        val error: MutedSettingsError? = null
    ) {
         sealed class MutedSettingsError {
             data class FailedToUnmuteUserError(val error: Throwable) : MutedSettingsError()
         }
    }

    sealed class UiEvent {
        data class RemovedFromMuteListEvent(val pubkey: String) : UiEvent()
    }
}