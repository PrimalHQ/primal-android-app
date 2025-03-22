package net.primal.android.auth.login

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.user.domain.LoginType
import net.primal.domain.nostr.NostrEvent

interface LoginContract {

    data class UiState(
        val loading: Boolean = false,
        val loginInput: String = "",
        val profileDetails: ProfileDetailsUi? = null,
        val fetchingProfileDetails: Boolean = false,
        val isValidKey: Boolean = false,
        val loginType: LoginType? = null,
        val error: LoginError? = null,
        val isExternalSignerInstalled: Boolean = false,
    ) {
        sealed class LoginError {
            data class GenericError(val cause: Throwable) : LoginError()
        }
    }

    sealed class UiEvent {
        data class LoginWithAmber(val pubkey: String) : UiEvent()
        data class LoginRequestEvent(val nostrEvent: NostrEvent? = null) : UiEvent()
        data class UpdateLoginInput(val newInput: String) : UiEvent()
    }

    sealed class SideEffect {
        data class RequestSign(val event: NostrUnsignedEvent) : SideEffect()
        data object LoginSuccess : SideEffect()
    }
}
