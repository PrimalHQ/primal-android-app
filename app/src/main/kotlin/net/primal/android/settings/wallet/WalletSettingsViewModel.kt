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
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.navigation.nwcUrl
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.NWCParseException
import net.primal.android.user.domain.WalletPreference
import net.primal.android.user.domain.parseNWCUrl
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.repository.WalletRepository
import timber.log.Timber

@HiltViewModel
class WalletSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WalletSettingsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: WalletSettingsContract.UiState.() -> WalletSettingsContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val events: MutableSharedFlow<WalletSettingsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: WalletSettingsContract.UiEvent) = viewModelScope.launch { events.emit(event) }

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
                    is WalletSettingsContract.UiEvent.DisconnectWallet -> disconnectWallet()
                    is WalletSettingsContract.UiEvent.UpdateWalletPreference -> {
                        updateWalletPreference(walletPreference = it.walletPreference)
                    }
                    is WalletSettingsContract.UiEvent.UpdateMinTransactionAmount -> {
                        updateSpamThresholdAmount(amountInSats = it.amountInSats)
                    }
                }
            }
        }

    private fun observeUserAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        wallet = it.nostrWallet,
                        walletPreference = it.walletPreference,
                        userLightningAddress = it.lightningAddress,
                        maxWalletBalanceInBtc = it.primalWalletSettings.maxBalanceInBtc,
                        spamThresholdAmountInSats = it.primalWalletSettings.spamThresholdAmountInSats,
                    )
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
                        walletPreference = WalletPreference.NostrWalletConnect,
                    )
                }
            } catch (error: NWCParseException) {
                Timber.e(error)
            }
        }

    private suspend fun disconnectWallet() {
        userRepository.disconnectNostrWallet(userId = activeAccountStore.activeUserId())
        setState {
            copy(
                wallet = null,
                userLightningAddress = null,
            )
        }
    }

    private fun updateWalletPreference(walletPreference: WalletPreference) =
        viewModelScope.launch {
            val activeUserId = activeAccountStore.activeUserId()
            userRepository.updateWalletPreference(userId = activeUserId, walletPreference = walletPreference)
            setState { copy(walletPreference = walletPreference) }
        }

    private fun updateSpamThresholdAmount(amountInSats: Long) =
        viewModelScope.launch {
            userRepository.updatePrimalWalletSettings(userId = activeAccountStore.activeUserId()) {
                this.copy(spamThresholdAmountInSats = amountInSats)
            }
            withContext(dispatcherProvider.io()) {
                walletRepository.deleteAllTransactions()
            }
        }
}
