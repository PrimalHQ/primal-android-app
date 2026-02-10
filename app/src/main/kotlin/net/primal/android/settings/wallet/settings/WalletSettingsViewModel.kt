package net.primal.android.settings.wallet.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
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
import net.primal.android.settings.wallet.settings.ui.model.asWalletNwcConnectionUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.utils.shouldShowBackup
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.primal.PrimalWalletNwcRepository
import net.primal.domain.parser.isNwcUrl
import net.primal.domain.usecase.ConnectNwcUseCase
import net.primal.domain.usecase.EnsurePrimalWalletExistsUseCase
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.WalletType
import net.primal.domain.wallet.capabilities
import net.primal.domain.wallet.nwc.NwcLogRepository

@Suppress("LongParameterList")
@HiltViewModel(assistedFactory = WalletSettingsViewModel.Factory::class)
class WalletSettingsViewModel @AssistedInject constructor(
    @Assisted private val nwcConnectionUrl: String?,
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val walletAccountRepository: WalletAccountRepository,
    private val primalWalletNwcRepository: PrimalWalletNwcRepository,
    private val nwcRepository: NwcRepository,
    private val connectNwcUseCase: ConnectNwcUseCase,
    private val nwcLogRepository: NwcLogRepository,
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

    private var connectionsJob: Job? = null

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

    private fun observeSparkConnections(): Job =
        viewModelScope.launch {
            nwcRepository.observeConnections(userId = activeAccountStore.activeUserId())
                .collect { connections ->
                    setState {
                        copy(
                            nwcConnectionsInfo = connections.map { it.asWalletNwcConnectionUi() },
                            connectionsState = WalletSettingsContract.ConnectionsState.Loaded,
                        )
                    }
                }
        }

    private fun fetchPrimalWalletConnections(wallet: Wallet.Primal): Job =
        viewModelScope.launch {
            if (wallet.kycLevel == WalletKycLevel.None) {
                setState { copy(connectionsState = WalletSettingsContract.ConnectionsState.Error) }
                return@launch
            }

            setState { copy(connectionsState = WalletSettingsContract.ConnectionsState.Loading) }
            runCatching {
                primalWalletNwcRepository.getConnections(userId = activeAccountStore.activeUserId())
                    .map { it.asWalletNwcConnectionUi() }
            }.onSuccess { connections ->
                setState {
                    copy(
                        nwcConnectionsInfo = connections,
                        connectionsState = WalletSettingsContract.ConnectionsState.Loaded,
                    )
                }
            }.onFailure { error ->
                Napier.w(throwable = error) { "Failed to fetch wallet connections." }
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

                    UiEvent.RequestFetchWalletConnections -> {
                        val wallet = state.value.activeWallet
                        if (wallet is Wallet.Primal) {
                            fetchPrimalWalletConnections(wallet)
                        }
                    }

                    is UiEvent.ConnectExternalWallet -> if (it.connectionLink.isNwcUrl()) {
                        connectWallet(nwcUrl = it.connectionLink)
                    }

                    UiEvent.RequestTransactionExport -> exportTransactions()

                    UiEvent.RevertToPrimalWallet -> revertToPrimalWallet()

                    UiEvent.RequestNwcLogsExport -> exportNwcLogs()
                }
            }
        }

    private fun revokeNwcConnection(nwcPubkey: String) =
        viewModelScope.launch {
            when (state.value.activeWallet) {
                is Wallet.Spark -> {
                    runCatching {
                        nwcRepository.revokeConnection(activeAccountStore.activeUserId(), nwcPubkey)
                    }.onFailure { error ->
                        Napier.w(throwable = error) { "Failed to revoke NWC connection." }
                    }
                }
                is Wallet.Primal -> {
                    val nwcConnections = state.value.nwcConnectionsInfo
                    val updatedConnections = nwcConnections.filterNot { it.nwcPubkey == nwcPubkey }
                    setState { copy(nwcConnectionsInfo = updatedConnections) }

                    runCatching {
                        primalWalletNwcRepository.revokeConnection(activeAccountStore.activeUserId(), nwcPubkey)
                    }.onFailure { error ->
                        Napier.w(throwable = error) { "Failed to revoke NWC connection." }
                        setState { copy(nwcConnectionsInfo = nwcConnections) }
                    }
                }
                else -> Unit
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

                    connectionsJob?.cancel()
                    connectionsJob = when (wallet) {
                        is Wallet.Spark -> observeSparkConnections()
                        is Wallet.Primal -> fetchPrimalWalletConnections(wallet)
                        else -> null
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
            runCatching {
                walletRepository.allTransactions(walletId = activeWalletId)
            }
                .onSuccess { transactions ->
                    setState {
                        copy(
                            isExportingTransactions = false,
                            transactionsToExport = transactions,
                        )
                    }
                    setEffect(SideEffect.TransactionsReadyForExport)
                }
                .onFailure {
                    Napier.e(throwable = it) { "Failed to export transactions." }
                    setState { copy(isExportingTransactions = false) }
                }
        }

    private fun exportNwcLogs() =
        viewModelScope.launch {
            setState { copy(isExportingNwcLogs = true) }
            runCatching {
                nwcLogRepository.getNwcLogs()
            }
                .onSuccess { logs ->
                    setState {
                        copy(
                            isExportingNwcLogs = false,
                            nwcLogsToExport = logs,
                        )
                    }
                    setEffect(SideEffect.NwcLogsReadyForExport)
                }
                .onFailure {
                    Napier.e(throwable = it) { "Failed to export NWC logs." }
                    setState { copy(isExportingNwcLogs = false) }
                }
        }

    private fun checkRevertToPrimalWalletAvailability() =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            primalWalletAccountRepository.fetchWalletStatus(userId)
                .onSuccess { status ->
                    setState {
                        copy(showRevertToPrimalWallet = status.hasCustodialWallet && !status.primalWalletDeprecated)
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

            // Fetch latest info to revert lightning address from local db
            sparkWalletAccountRepository.fetchWalletAccountInfo(userId = userId, sparkWallet.walletId)

            // Clear migration state so it can be re-run if user migrates again
            sparkWalletAccountRepository.clearPrimalTxsMigrationState(sparkWallet.walletId)

            // Ensure Primal wallet exists and set as active
            ensurePrimalWalletExistsUseCase.invoke(userId = userId, setAsActive = true)
                .onFailure { error ->
                    Napier.e(throwable = error) { "Failed to restore Primal wallet." }
                }

            setState { copy(isRevertingToPrimalWallet = false, showRevertToPrimalWallet = false) }
        }
}
