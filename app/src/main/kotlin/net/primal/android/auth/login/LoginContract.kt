package net.primal.android.auth.login

interface LoginContract {

    data class UiState(
        val loading: Boolean = false,
    )

    sealed class UiEvent {
        data class LoginEvent(val nsec: String) : UiEvent()
    }

    sealed class SideEffect {
        data class LoginSuccess(val pubkey: String) : SideEffect()
    }
}
