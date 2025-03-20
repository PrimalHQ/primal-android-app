package net.primal.android.auth.login

import android.content.Intent
import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface LoginContract {

    data class UiState(
        val loading: Boolean = false,
        val loginInput: String = "",
        val profileDetails: ProfileDetailsUi? = null,
        val fetchingProfileDetails: Boolean = false,
        val isValidKey: Boolean = false,
        val isNpubLogin: Boolean? = null,
        val error: LoginError? = null,
        val isExternalSignerInstalled: Boolean = false,
    ) {
        sealed class LoginError {
            data class GenericError(val cause: Throwable) : LoginError()
        }
    }

    sealed class UiEvent {
        data object LoginWithAmber : UiEvent()
        data object LoginRequestEvent : UiEvent()
        data class UpdateLoginInput(val newInput: String) : UiEvent()
    }

    sealed class SideEffect {
        data class AskForSign(val intent: Intent) : SideEffect()
        data object LoginSuccess : SideEffect()
    }
}
