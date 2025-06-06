package net.primal.android.settings.wallet.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.settings.wallet.domain.NwcConnectionInfo
import net.primal.android.settings.wallet.settings.WalletSettingsContract.UiEvent
import net.primal.android.settings.wallet.settings.WalletSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.NWCParseException
import net.primal.android.user.domain.WalletPreference
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.api.model.PrimalNwcConnectionInfo
import net.primal.android.wallet.domain.WalletKycLevel
import net.primal.android.wallet.repository.NwcWalletRepository
import net.primal.android.wallet.repository.WalletRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import timber.log.Timber

@HiltViewModel(assistedFactory = WalletSettingsViewModel.Factory::class)
class WalletSettingsViewModel @AssistedInject constructor(
    @Assisted private val nwcConnectionUrl: String?,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository,
    private val nwcWalletRepository: NwcWalletRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(nwcConnectionUrl: String?): WalletSettingsViewModel
    }

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        if (nwcConnectionUrl != null) {
            connectWallet(nwcUrl = nwcConnectionUrl)
        } else {
            observeUserAccount()
        }

        observeEvents()
        observeActiveUserAccount()
    }

    private fun observeActiveUserAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                val isWalletActivated = it.primalWallet != null && it.primalWallet.kycLevel != WalletKycLevel.None
                setState { copy(isPrimalWalletActivated = isWalletActivated) }
            }
        }

    private fun fetchWalletConnections() =
        viewModelScope.launch {
            try {
                setState { copy(connectionsState = WalletSettingsContract.ConnectionsState.Loading) }
                val response = nwcWalletRepository.getConnections(userId = activeAccountStore.activeUserId())
                setState {
                    copy(
                        nwcConnectionsInfo = response.map { it.mapAsConnectedAppUi() },
                        connectionsState = WalletSettingsContract.ConnectionsState.Loaded,
                    )
                }
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(connectionsState = WalletSettingsContract.ConnectionsState.Error) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.DisconnectWallet -> disconnectWallet()
                    is UiEvent.UpdateWalletPreference -> {
                        updateWalletPreference(walletPreference = it.walletPreference)
                    }

                    is UiEvent.UpdateMinTransactionAmount -> {
                        updateSpamThresholdAmount(amountInSats = it.amountInSats)
                    }

                    is UiEvent.RevokeConnection -> {
                        revokeNwcConnection(nwcPubkey = it.nwcPubkey)
                    }

                    UiEvent.RequestFetchWalletConnections -> fetchWalletConnections()

                    is UiEvent.ConnectExternalWallet -> if (it.connectionLink.isNwcUrl()) {
                        connectWallet(nwcUrl = it.connectionLink)
                    }
                }
            }
        }

    private fun revokeNwcConnection(nwcPubkey: String) =
        viewModelScope.launch {
            val nwcConnections = state.value.nwcConnectionsInfo
            try {
                val updatedConnections = nwcConnections.filterNot { it.nwcPubkey == nwcPubkey }
                setState { copy(nwcConnectionsInfo = updatedConnections) }
                nwcWalletRepository.revokeConnection(activeAccountStore.activeUserId(), nwcPubkey)
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(nwcConnectionsInfo = nwcConnections) }
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
                Timber.w(error)
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
            walletRepository.deleteAllTransactions(userId = activeAccountStore.activeUserId())
        }

    private fun PrimalNwcConnectionInfo.mapAsConnectedAppUi(): NwcConnectionInfo {
        return NwcConnectionInfo(
            nwcPubkey = nwcPubkey,
            appName = appName,
            dailyBudget = dailyBudget,
        )
    }
}
