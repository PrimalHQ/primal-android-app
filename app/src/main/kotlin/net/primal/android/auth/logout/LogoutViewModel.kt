package net.primal.android.auth.logout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.auth.logout.LogoutContract.SideEffect
import net.primal.android.auth.logout.LogoutContract.UiEvent
import net.primal.android.auth.repository.AuthRepository
import net.primal.android.navigation.profileIdOrThrow
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    activeAccountStore: ActiveAccountStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val profileId = savedStateHandle.profileIdOrThrow
    private val isActiveAccount = profileId == activeAccountStore.activeUserId()

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.LogoutConfirmed -> logout()
                }
            }
        }

    private suspend fun logout() {
        runCatching {
            authRepository.logout(pubkey = profileId)
        }.onSuccess {
            if (isActiveAccount) {
                setEffect(SideEffect.ActiveAccountLogoutSuccessful)
            } else {
                setEffect(SideEffect.UserAccountLogoutSuccessful)
            }
        }
    }
}
