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
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl
import net.primal.android.wallet.repository.NwcWalletRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.wallet.NostrWalletKeypair
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.remote.model.PrimalNwcConnectionInfo
import timber.log.Timber

@HiltViewModel(assistedFactory = WalletSettingsViewModel.Factory::class)
class WalletSettingsViewModel @AssistedInject constructor(
    @Assisted private val nwcConnectionUrl: String?,
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val walletAccountRepository: WalletAccountRepository,
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
            observeActiveWallet()
        }

        observeEvents()
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
                    is UiEvent.UpdatePreferPrimalWallet -> {
                        updatePreferPrimalWallet(preferPrimalWallet = it.value)
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

    private fun observeActiveWallet() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .collect { setState { copy(wallet = it, preferPrimalWallet = it is Wallet.Primal) } }
        }

    private fun connectWallet(nwcUrl: String) =
        viewModelScope.launch {
            try {
                val nostrWalletConnect = nwcUrl.parseNWCUrl()
                val lightningAddress = activeAccountStore.activeUserAccount().lightningAddress

                walletRepository.upsertNostrWallet(
                    userId = activeAccountStore.activeUserId(),
                    wallet = Wallet.NWC(
                        walletId = nostrWalletConnect.pubkey,
                        userId = activeAccountStore.activeUserId(),
                        lightningAddress = lightningAddress,
                        balanceInBtc = null,
                        maxBalanceInBtc = null,
                        spamThresholdAmountInSats = 1L,
                        lastUpdatedAt = null,
                        relays = nostrWalletConnect.relays,
                        keypair = NostrWalletKeypair(
                            privateKey = nostrWalletConnect.keypair.privateKey,
                            pubKey = nostrWalletConnect.keypair.pubkey,
                        ),
                    ),
                )

                walletAccountRepository.setActiveWallet(
                    userId = activeAccountStore.activeUserId(),
                    walletId = nostrWalletConnect.pubkey,
                )
                setState { copy(preferPrimalWallet = false) }
            } catch (error: NWCParseException) {
                Timber.w(error)
            }
        }

    private suspend fun disconnectWallet() {
        walletAccountRepository.clearActiveWallet(userId = activeAccountStore.activeUserId())
    }

    private fun updatePreferPrimalWallet(preferPrimalWallet: Boolean) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            if (preferPrimalWallet) {
                walletAccountRepository.setActiveWallet(userId = userId, walletId = userId)
            } else {
                val lastUsedNWC = walletAccountRepository.findLastUsedNostrWallet(userId = userId)
                if (lastUsedNWC != null) {
                    walletAccountRepository.setActiveWallet(userId = userId, walletId = lastUsedNWC.walletId)
                } else {
                    walletAccountRepository.clearActiveWallet(userId = userId)
                }
            }
        }

    private fun updateSpamThresholdAmount(amountInSats: Long) =
        viewModelScope.launch {
            walletRepository.upsertWalletSettings(
                walletId = state.value.wallet?.walletId ?: activeAccountStore.activeUserId(),
                spamThresholdAmountInSats = amountInSats,
            )
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
