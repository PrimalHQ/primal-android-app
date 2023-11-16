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
import net.primal.android.auth.AuthRepository
import net.primal.android.auth.login.LoginContract.SideEffect
import net.primal.android.auth.login.LoginContract.UiEvent
import net.primal.android.auth.login.LoginContract.UiState
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.UserRepository

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(loading = false))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) =
        viewModelScope.launch {
            _effect.send(effect)
        }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.LoginEvent -> login(nostrKey = it.nostrKey)
                }
            }
        }

    private fun login(nostrKey: String) =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val pubkey = authRepository.login(nostrKey = nostrKey)
                userRepository.fetchAndUpdateUserAccount(userId = pubkey)
                settingsRepository.fetchAndPersistAppSettings(userId = pubkey)
                mutedUserRepository.fetchAndPersistMuteList(userId = pubkey)
                setEffect(SideEffect.LoginSuccess(pubkey = pubkey))
            } catch (error: WssException) {
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
}
