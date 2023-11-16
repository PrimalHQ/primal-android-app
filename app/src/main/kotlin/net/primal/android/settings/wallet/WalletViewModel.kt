package net.primal.android.settings.wallet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.nwcUrl
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.NWCParseException
import net.primal.android.user.domain.parseNWCUrl
import net.primal.android.user.repository.UserRepository

@HiltViewModel
class WalletViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WalletContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: WalletContract.UiState.() -> WalletContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val events: MutableSharedFlow<WalletContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: WalletContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        val nwcConnectionUrl = savedStateHandle.nwcUrl
        if (nwcConnectionUrl != null) {
            connectWallet(nwcUrl = nwcConnectionUrl)
        } else {
            observeUserAccount()
        }

        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is WalletContract.UiEvent.DisconnectWallet -> disconnectWallet()
                }
            }
        }

    private fun observeUserAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                val nostrWalletConnect = activeAccountStore.activeUserAccount().nostrWallet
                val lightningAddress = activeAccountStore.activeUserAccount().lightningAddress

                if (nostrWalletConnect != null) {
                    setState {
                        copy(
                            wallet = nostrWalletConnect,
                            userLightningAddress = lightningAddress,
                        )
                    }
                }
            }
        }

    private fun connectWallet(nwcUrl: String) =
        viewModelScope.launch {
            try {
                val nostrWalletConnect = nwcUrl.parseNWCUrl()
                val lightningAddress = activeAccountStore.activeUserAccount().lightningAddress

                userRepository.connectNostrWallet(
                    userId = activeAccountStore.activeUserId(),
                    nostrWalletConnect = nostrWalletConnect,
                )

                setState {
                    copy(
                        wallet = nostrWalletConnect,
                        userLightningAddress = lightningAddress,
                    )
                }
            } catch (error: NWCParseException) {
                // Propagate error to the UI
            }
        }

    private suspend fun disconnectWallet() {
        userRepository.disconnectNostrWallet(
            userId = activeAccountStore.activeUserId(),
        )

        setState {
            copy(
                wallet = null,
                userLightningAddress = null,
            )
        }
    }
}
