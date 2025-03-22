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
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.isValidNostrPrivateKey
import net.primal.android.core.utils.isValidNostrPublicKey
import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.crypto.extractKeyPairFromPrivateKeyOrThrow
import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.ext.extractProfileId
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.domain.LoginType
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.serialization.CommonJson
import net.primal.data.remote.api.settings.model.AppSettingsDescription
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.asIdentifierTag
import timber.log.Timber

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
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

                    is UiEvent.UpdateLoginInput -> changeLoginInput(input = it.newInput)
                    is UiEvent.LoginWithAmber -> loginWithAmber(pubkey = it.pubkey)
                }
            }
        }

    private fun loginWithAmber(pubkey: String) =
        viewModelScope.launch {
            changeLoginInput(input = pubkey, loginType = LoginType.ExternalSigner)
            setEffect(
                SideEffect.RequestSign(
                    event = NostrUnsignedEvent(
                        pubKey = pubkey.extractProfileId() ?: "",
                        kind = NostrEventKind.ApplicationSpecificData.value,
                        tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                        content = CommonJson.encodeToString(
                            AppSettingsDescription(description = "Sync app settings"),
                        ),
                    ),
                ),
            )
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
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = UiState.LoginError.GenericError(error))
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
            val userId = npub.bech32ToHexOrThrow()
            val profile = withContext(dispatcherProvider.io()) {
                try {
                    profileRepository.requestProfileUpdate(profileId = userId)
                    profileRepository.findProfileDataOrNull(profileId = userId)
                } catch (error: WssException) {
                    Timber.w(error)
                    null
                }
            }
            setState { copy(fetchingProfileDetails = false, profileDetails = profile?.asProfileDetailsUi()) }
        }
}
