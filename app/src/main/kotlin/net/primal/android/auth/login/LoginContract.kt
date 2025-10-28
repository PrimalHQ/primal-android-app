package net.primal.android.auth.login

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.user.domain.CredentialType
import net.primal.domain.nostr.NostrEvent

interface LoginContract {

    data class UiState(
        val loading: Boolean = false,
        val loginInput: String = "",
        val profileDetails: ProfileDetailsUi? = null,
        val fetchingProfileDetails: Boolean = false,
        val isValidKey: Boolean = false,
        val credentialType: CredentialType? = null,
        val error: LoginError? = null,
        val isExternalSignerInstalled: Boolean = false,
    ) {
        sealed class LoginError {
            data class GenericError(val cause: Throwable) : LoginError()
        }
    }

    sealed class UiEvent {
        data object ResetLoginState : UiEvent()
        data class LoginRequestEvent(val nostrEvent: NostrEvent? = null) : UiEvent()
        data class UpdateLoginInput(val newInput: String, val credentialType: CredentialType? = null) : UiEvent()
    }

    sealed class SideEffect {
        data object LoginSuccess : SideEffect()
    }

    data class ScreenCallbacks(
        val onLoginSuccess: () -> Unit,
        val onClose: () -> Unit,
    )
}
