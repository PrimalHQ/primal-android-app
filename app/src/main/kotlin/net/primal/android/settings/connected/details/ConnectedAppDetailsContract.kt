package net.primal.android.settings.connected.details

import net.primal.domain.account.model.AppConnection

interface ConnectedAppDetailsContract {
    data class UiState(
        val connection: AppConnection? = null,
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data object RemoveConnection : UiEvent()
    }
}
