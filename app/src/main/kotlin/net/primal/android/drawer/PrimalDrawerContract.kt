package net.primal.android.drawer

import net.primal.android.user.domain.UserAccount

interface PrimalDrawerContract {

    data class UiState(
        val menuItems: List<DrawerScreenDestination>,
        val loading: Boolean = false,
        val activeUserAccount: UserAccount? = null,
    )

    sealed class UiEvent {
        data class ThemeSwitchClick(val isSystemInDarkTheme: Boolean) : UiEvent()
    }
}
