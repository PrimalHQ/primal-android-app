package net.primal.android.auth.logout

interface LogoutContract {

    sealed class UiEvent {
        object LogoutConfirmed : UiEvent()
    }
}
