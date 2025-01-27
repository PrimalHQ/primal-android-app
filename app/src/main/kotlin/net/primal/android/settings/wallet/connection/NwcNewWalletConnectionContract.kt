package net.primal.android.settings.wallet.connection

interface NwcNewWalletConnectionContract {
    data class UiState(
        val loading: Boolean = false,
        val appName: String = "",
    )

    sealed class UiEvent {
        data class AppNameChangedEvent(val appName: String) : UiEvent()
        data object CreateWalletConnection : UiEvent()
    }

    sealed class SideEffect
}
