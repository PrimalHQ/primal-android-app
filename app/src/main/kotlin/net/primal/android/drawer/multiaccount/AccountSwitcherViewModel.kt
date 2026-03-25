package net.primal.android.drawer.multiaccount

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
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.domain.profile.Nip05VerificationService

@HiltViewModel
class AccountSwitcherViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val accountsStore: UserAccountsStore,
    private val nip05VerificationService: Nip05VerificationService,
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
                .collect { activeAccount ->
                    val nip05Status = nip05VerificationService.getStatus(activeAccount.pubkey)
                    setState {
                        copy(
                            userAccounts = userAccounts.sortAndFilterAccounts(activeUserId = activeAccount.pubkey),
                            activeAccount = activeAccount.asUserAccountUi().copy(nip05Status = nip05Status),
                        )
                    }
                }
        }

    private fun observeUserAccounts() =
        viewModelScope.launch {
            accountsStore.userAccounts
                .collect { accounts ->
                    val statuses = nip05VerificationService.getStatuses(accounts.map { it.pubkey })
                    setState {
                        copy(
                            userAccounts = accounts.map {
                                it.asUserAccountUi().copy(nip05Status = statuses[it.pubkey])
                            }.sortAndFilterAccounts(activeUserId = activeAccount?.pubkey),
                        )
                    }
                }
        }

    private fun List<UserAccountUi>.sortAndFilterAccounts(activeUserId: String?) =
        sortedByDescending { it.lastAccessedAt }
            .filterNot { it.pubkey == activeUserId }
}
