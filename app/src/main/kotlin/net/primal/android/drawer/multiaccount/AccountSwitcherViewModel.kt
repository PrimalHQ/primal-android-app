package net.primal.android.drawer.multiaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository

@HiltViewModel
class AccountSwitcherViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val accountsStore: UserAccountsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountSwitcherContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: AccountSwitcherContract.UiState.() -> AccountSwitcherContract.UiState) =
        _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<AccountSwitcherContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: AccountSwitcherContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<AccountSwitcherContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: AccountSwitcherContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeActiveAccount()
        observeUserAccounts()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is AccountSwitcherContract.UiEvent.SwitchAccount -> setActiveAccount(userId = it.userId)
                }
            }
        }

    private fun setActiveAccount(userId: String) =
        viewModelScope.launch {
            userRepository.setActiveAccount(userId = userId)
            setEffect(AccountSwitcherContract.SideEffect.AccountSwitched)
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .distinctUntilChanged()
                .collect { activeAccount ->
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
            accountsStore.userAccounts
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
