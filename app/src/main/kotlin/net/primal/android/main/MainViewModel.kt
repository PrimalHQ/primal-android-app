package net.primal.android.main

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
import net.primal.android.core.updater.DataUpdater
import net.primal.android.main.MainContract.UiEvent
import net.primal.android.main.MainContract.UiState
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.android.user.subscriptions.SubscriptionsManager

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataUpdater: DataUpdater,
    private val activeAccountStore: ActiveAccountStore,
    private val accountsStore: UserAccountsStore,
    private val userRepository: UserRepository,
    private val subscriptionsManager: SubscriptionsManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<MainContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: MainContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeActiveAccount()
        observeUserAccounts()
        observeBadgesUpdates()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RequestUserDataUpdate -> dataUpdater.updateData()
                    UiEvent.SwitchToNextAccount -> switchToNextAccount()
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        activeAccountLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                        activeAccountBlossoms = it.blossomServers,
                    )
                }
            }
        }

    private fun observeUserAccounts() =
        viewModelScope.launch {
            accountsStore.userAccounts.collect { accounts ->
                setState { copy(hasMultipleAccounts = accounts.size > 1) }
            }
        }

    private fun switchToNextAccount() =
        viewModelScope.launch {
            val activeUserId = activeAccountStore.activeUserId()
            val nextAccount = accountsStore.userAccounts.value
                .filter { it.pubkey != activeUserId }
                .maxByOrNull { it.lastAccessedAt }
                ?: return@launch
            userRepository.setActiveAccount(userId = nextAccount.pubkey)
            setEffect(MainContract.SideEffect.AccountSwitched)
        }

    private fun observeBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }
}
