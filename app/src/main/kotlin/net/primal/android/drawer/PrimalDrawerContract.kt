package net.primal.android.drawer

interface PrimalDrawerContract {

    data class UiState(
        val loading: Boolean = false,
    )

    sealed class UiEvent {
        data class ThemeSwitchClick(val isSystemInDarkTheme: Boolean) : UiEvent()
    }
}