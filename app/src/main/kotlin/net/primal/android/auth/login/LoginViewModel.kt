package net.primal.android.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.auth.login.LoginContract.SideEffect
import net.primal.android.auth.login.LoginContract.UiEvent
import net.primal.android.auth.login.LoginContract.UiState
import net.primal.android.auth.repository.LoginHandler
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.user.domain.LoginType
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.utils.extractKeyPairFromPrivateKeyOrThrow
import net.primal.domain.nostr.utils.isValidNostrPrivateKey
import net.primal.domain.nostr.utils.isValidNostrPublicKey
import net.primal.domain.nostr.utils.takeAsProfileHexIdOrNull
import net.primal.domain.profile.ProfileRepository
import timber.log.Timber

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val profileRepository: ProfileRepository,
    private val loginHandler: LoginHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(loading = false))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.LoginRequestEvent ->
                        login(nostrKey = _state.value.loginInput, authorizationEvent = it.nostrEvent)

                    is UiEvent.UpdateLoginInput -> changeLoginInput(input = it.newInput, loginType = it.loginType)
                    UiEvent.ResetLoginState -> resetLoginState()
                }
            }
        }

    private fun login(nostrKey: String, authorizationEvent: NostrEvent?) =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                state.value.loginType?.let { loginType ->
                    loginHandler.login(
                        nostrKey = nostrKey,
                        loginType = loginType,
                        authorizationEvent = authorizationEvent,
                    )
                    setEffect(SideEffect.LoginSuccess)
                }
            } catch (error: NetworkException) {
                Timber.w(error)
                setErrorState(error = UiState.LoginError.GenericError(error))
                if (state.value.loginType == LoginType.ExternalSigner) {
                    resetLoginState()
                }
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun setErrorState(error: UiState.LoginError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }

    private fun resetLoginState() = changeLoginInput(input = "")

    private fun changeLoginInput(input: String, loginType: LoginType? = null) {
        setState { copy(loginInput = input) }
        viewModelScope.launch {
            when {
                input.isValidNostrPrivateKey() -> {
                    setState { copy(isValidKey = true, loginType = loginType ?: LoginType.PrivateKey) }
                    val (_, npub) = input.extractKeyPairFromPrivateKeyOrThrow()
                    fetchProfileDetails(npub = npub)
                }

                input.isValidNostrPublicKey() -> {
                    setState { copy(isValidKey = true, loginType = loginType ?: LoginType.PublicKey) }
                    fetchProfileDetails(npub = input)
                }

                else -> {
                    setState {
                        copy(
                            fetchingProfileDetails = false,
                            profileDetails = null,
                            isValidKey = false,
                            loginType = null,
                        )
                    }
                }
            }
        }
    }

    private fun fetchProfileDetails(npub: String) =
        viewModelScope.launch {
            setState { copy(fetchingProfileDetails = true) }
            val profile = withContext(dispatcherProvider.io()) {
                try {
                    npub.takeAsProfileHexIdOrNull()?.let { userId ->
                        profileRepository.fetchProfile(profileId = userId)
                        profileRepository.findProfileDataOrNull(profileId = userId)
                    }
                } catch (error: NetworkException) {
                    Timber.w(error)
                    null
                }
            }
            setState { copy(fetchingProfileDetails = false, profileDetails = profile?.asProfileDetailsUi()) }
        }
}
