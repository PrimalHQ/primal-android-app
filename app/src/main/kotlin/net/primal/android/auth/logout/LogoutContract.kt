package net.primal.android.auth.logout

interface LogoutContract {

    sealed class UiEvent {
        object LogoutConfirmed : UiEvent()
    }

    sealed class SideEffect {
        data object UserAccountLogoutSuccessful : SideEffect()
        data class ActiveAccountLogoutSuccessful(val isLastAccount: Boolean) : SideEffect()
    }
}
