package net.primal.android.auth.logout

interface LogoutContract {

    sealed class UiEvent {
        object LogoutConfirmed : UiEvent()
    }

    sealed class SideEffect {
        data object UserAccountLogoutSuccessful : SideEffect()
        data object ActiveAccountLogoutSuccessful : SideEffect()
    }
}
