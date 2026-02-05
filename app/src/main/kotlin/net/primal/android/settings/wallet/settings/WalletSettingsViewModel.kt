package net.primal.android.settings.wallet.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.service.PrimalNwcService
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.settings.wallet.settings.WalletSettingsContract.SideEffect
import net.primal.android.settings.wallet.settings.WalletSettingsContract.UiEvent
import net.primal.android.settings.wallet.settings.WalletSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.utils.shouldShowBackup
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.connections.primal.PrimalWalletNwcRepository
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.parser.isNwcUrl
import net.primal.domain.usecase.ConnectNwcUseCase
import net.primal.domain.usecase.EnsurePrimalWalletExistsUseCase
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.WalletType
import net.primal.domain.wallet.capabilities

@Suppress("LongParameterList")
@HiltViewModel(assistedFactory = WalletSettingsViewModel.Factory::class)
class WalletSettingsViewModel @AssistedInject constructor(
    @Assisted private val nwcConnectionUrl: String?,
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val walletAccountRepository: WalletAccountRepository,
    private val primalWalletNwcRepository: PrimalWalletNwcRepository,
    private val connectNwcUseCase: ConnectNwcUseCase,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val ensurePrimalWalletExistsUseCase: EnsurePrimalWalletExistsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(nwcConnectionUrl: String?): WalletSettingsViewModel
    }

    private val _state = MutableStateFlow(UiState(activeUserId = activeAccountStore.activeUserId()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        if (nwcConnectionUrl != null) {
            connectWallet(nwcUrl = nwcConnectionUrl)
        } else {
            observeActiveWalletData()
            observeActiveWalletId()
        }

        observeActiveAccount()
        observeServiceRunningState()
        observeEvents()
    }

    private fun fetchWalletConnections() =
        viewModelScope.launch {
            try {
                setState { copy(connectionsState = WalletSettingsContract.ConnectionsState.Loading) }
                val connections = primalWalletNwcRepository.getConnections(userId = activeAccountStore.activeUserId())
                setState {
                    copy(
                        nwcConnectionsInfo = connections,
                        connectionsState = WalletSettingsContract.ConnectionsState.Loaded,
                    )
                }
            } catch (error: SignatureException) {
                Napier.w(throwable = error) { "Failed to fetch wallet connections due to signature error." }
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Failed to fetch wallet connections due to network error." }
                setState { copy(connectionsState = WalletSettingsContract.ConnectionsState.Error) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.DisconnectWallet -> disconnectWallet()
                    is UiEvent.UpdateUseExternalWallet -> {
                        updateUseExternalWallet(useExternalWallet = it.value)
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

                    UiEvent.RequestTransactionExport -> exportTransactions()

                    UiEvent.RevertToPrimalWallet -> revertToPrimalWallet()
                }
            }
        }

    private fun revokeNwcConnection(nwcPubkey: String) =
        viewModelScope.launch {
            val nwcConnections = state.value.nwcConnectionsInfo
            try {
                val updatedConnections = nwcConnections.filterNot { it.nwcPubkey == nwcPubkey }
                setState { copy(nwcConnectionsInfo = updatedConnections) }
                primalWalletNwcRepository.revokeConnection(activeAccountStore.activeUserId(), nwcPubkey)
            } catch (error: SignatureException) {
                Napier.w(throwable = error) { "Failed to revoke NWC connection due to signature error." }
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Failed to revoke NWC connection due to network error." }
                setState { copy(nwcConnectionsInfo = nwcConnections) }
            }
        }

    private fun observeActiveWalletId() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWalletId(userId = activeAccountStore.activeUserId())
                .collect { walletId ->
                    if (walletId != null) {
                        walletRepository.fetchWalletBalance(walletId = walletId)
                    }
                }
        }

    private fun observeActiveWalletData() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .collect { wallet ->
                    val shouldShowBackup = wallet.shouldShowBackup
                    setState {
                        copy(
                            activeWallet = wallet,
                            useExternalWallet = wallet == null || wallet is Wallet.NWC,
                            showBackupWidget = shouldShowBackup,
                            showBackupListItem = wallet?.capabilities?.supportsWalletBackup == true &&
                                !shouldShowBackup,
                        )
                    }

                    if (wallet is Wallet.Spark) {
                        checkRevertToPrimalWalletAvailability()
                    } else {
                        setState { copy(showRevertToPrimalWallet = false) }
                    }
                }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeUserId = it.pubkey,
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        activeAccountLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                        activeAccountBlossoms = it.blossomServers,
                        activeAccountDisplayName = it.authorDisplayName,
                    )
                }
            }
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeServiceRunningState() =
        viewModelScope.launch {
            activeAccountStore.activeUserId
                .flatMapLatest { userId ->
                    PrimalNwcService.isRunningForUser(userId)
                }
                .collect { isRunning ->
                    setState { copy(isServiceRunningForCurrentUser = isRunning) }
                }
        }

    private fun connectWallet(nwcUrl: String) =
        viewModelScope.launch {
            connectNwcUseCase.invoke(
                userId = activeAccountStore.activeUserId(),
                nwcUrl = nwcUrl,
                autoSetAsDefaultWallet = true,
            )
                .onFailure { Napier.w(throwable = it) { "Failed to connect wallet." } }
                .onSuccess { setState { copy(useExternalWallet = true) } }
        }

    private suspend fun disconnectWallet() {
        state.value.activeWallet?.walletId?.let { walletId ->
            walletRepository.deleteWalletById(walletId = walletId)
        }
        walletAccountRepository.clearActiveWallet(userId = activeAccountStore.activeUserId())
    }

    private fun updateUseExternalWallet(useExternalWallet: Boolean) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            if (useExternalWallet) {
                val lastUsedNWC = walletAccountRepository.findLastUsedWallet(userId = userId, type = WalletType.NWC)
                if (lastUsedNWC != null) {
                    walletAccountRepository.setActiveWallet(userId = userId, walletId = lastUsedNWC.walletId)
                } else {
                    walletAccountRepository.clearActiveWallet(userId = userId)
                }
            } else {
                val lastUsedInternalWallet = walletAccountRepository.findLastUsedWallet(
                    userId = userId,
                    type = setOf(WalletType.PRIMAL, WalletType.SPARK),
                )

                if (lastUsedInternalWallet != null) {
                    walletAccountRepository.setActiveWallet(userId = userId, walletId = lastUsedInternalWallet.walletId)
                } else {
                    walletAccountRepository.setActiveWallet(userId = userId, walletId = userId)
                }
            }
        }

    private fun updateSpamThresholdAmount(amountInSats: Long) =
        viewModelScope.launch {
            walletRepository.upsertWalletSettings(
                walletId = state.value.activeWallet?.walletId ?: activeAccountStore.activeUserId(),
                spamThresholdAmountInSats = amountInSats,
            )
            walletRepository.deleteAllTransactions(userId = activeAccountStore.activeUserId())
        }

    private fun exportTransactions() =
        viewModelScope.launch {
            val activeWalletId = state.value.activeWallet?.walletId ?: return@launch
            setState { copy(isExportingTransactions = true) }

            val transactions = walletRepository.allTransactions(walletId = activeWalletId)
            setState {
                copy(
                    isExportingTransactions = false,
                    transactionsToExport = transactions,
                )
            }
            setEffect(SideEffect.TransactionsReadyForExport)
        }

    private fun checkRevertToPrimalWalletAvailability() =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            primalWalletAccountRepository.fetchWalletStatus(userId)
                .onSuccess { status ->
                    setState {
                        copy(showRevertToPrimalWallet = status.hasCustodialWallet)
                    }
                }
                .onFailure {
                    Napier.w(throwable = it) { "Failed to check wallet status for revert option." }
                    setState { copy(showRevertToPrimalWallet = false) }
                }
        }

    private fun revertToPrimalWallet() =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            val sparkWallet = state.value.activeWallet as? Wallet.Spark ?: return@launch

            setState { copy(isRevertingToPrimalWallet = true) }

            // Unregister Spark wallet
            sparkWalletAccountRepository.unregisterSparkWallet(userId, sparkWallet.walletId)
                .onFailure { error ->
                    Napier.e(throwable = error) { "Failed to unregister Spark wallet." }
                    setState { copy(isRevertingToPrimalWallet = false) }
                    return@launch
                }

            // Ensure Primal wallet exists and set as active
            ensurePrimalWalletExistsUseCase.invoke(userId = userId, setAsActive = true)
                .onFailure { error ->
                    Napier.e(throwable = error) { "Failed to restore Primal wallet." }
                }

            setState { copy(isRevertingToPrimalWallet = false, showRevertToPrimalWallet = false) }
        }
}
