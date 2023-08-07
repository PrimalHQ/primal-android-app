package net.primal.android.auth.login

interface LoginContract {

    data class UiState(
        val loading: Boolean = false,
        val error: ApiError? = null,
    ) {
        sealed class ApiError {
            object GenericError : ApiError()
        }
    }

    sealed class UiEvent {
        data class LoginEvent(val nostrKey: String) : UiEvent()
    }

    sealed class SideEffect {
        data class LoginSuccess(val pubkey: String) : SideEffect()
    }
}
