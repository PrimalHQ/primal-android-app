package net.primal.android.multiaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.auth.repository.AuthRepository
import net.primal.android.multiaccount.AccountSwitcherContract.SideEffect
import net.primal.android.multiaccount.AccountSwitcherContract.UiEvent
import net.primal.android.multiaccount.AccountSwitcherContract.UiState
import net.primal.android.multiaccount.model.asUserAccountUi

@HiltViewModel
class AccountSwitcherViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeActiveAccount()
        observeUserAccounts()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SwitchAccount -> setActiveAccount(userId = it.userId)
                }
            }
        }

    private fun setActiveAccount(userId: String) =
        viewModelScope.launch {
            authRepository.setActiveAccount(userId = userId)
            setEffect(SideEffect.AccountSwitched)
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            authRepository.observeActiveAccount().collect { activeAccount ->
                setState {
                    copy(
                        userAccounts = listOfNotNull(this.activeAccount) +
                            userAccounts.filterNot { it.pubkey == activeAccount.pubkey },
                        activeAccount = activeAccount.asUserAccountUi(),
                    )
                }
            }
        }

    private fun observeUserAccounts() =
        viewModelScope.launch {
            authRepository.observeUserAccounts()
                .collect {
                    setState {
                        copy(
                            userAccounts = it.map { it.asUserAccountUi() }
                                .sortedByDescending { it.lastAccessedAt }
                                .filterNot { it.pubkey == activeAccount?.pubkey },
                        )
                    }
                }
        }
}
