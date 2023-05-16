package net.primal.android.login

interface LoginContract {

    data class UiState(
        val loading: Boolean = false,
    )

    sealed class UiEvent {

    }

    sealed class SideEffect {

    }
}