package net.primal.android.auth.logout

interface LogoutContract {

    sealed class UiEvent {
        data object LogoutConfirmed : UiEvent()
    }

    sealed class SideEffect {
        data object Close : SideEffect()
        data object CleanupAndNavigateToWelcome : SideEffect()
        data object NavigateToHome : SideEffect()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val navigateToHome: () -> Unit,
        val navigateToWelcome: () -> Unit,
    )
}
