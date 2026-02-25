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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.push.PushNotificationsTokenUpdater
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
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.connections.nostr.NwcRepository
import net.primal.domain.connections.primal.PrimalWalletNwcRepository
import net.primal.domain.parser.isNwcUrl
import net.primal.domain.usecase.ConnectNwcUseCase
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.WalletType
import net.primal.domain.wallet.capabilities
import net.primal.domain.wallet.nwc.NwcLogRepository
import net.primal.wallet.data.repository.handler.MigratePrimalTransactionsHandler

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
    private val pushNotificationsTokenUpdater: PushNotificationsTokenUpdater,
    private val migratePrimalTransactionsHandler: MigratePrimalTransactionsHandler,
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
        observeAutoStartState()
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
                            connectionsJob?.cancel()
                            connectionsJob = fetchPrimalWalletConnections(wallet)
                        }
                    }

                    is UiEvent.ConnectExternalWallet -> if (it.connectionLink.isNwcUrl()) {
                        connectWallet(nwcUrl = it.connectionLink)
                    }

                    UiEvent.RequestTransactionExport -> exportTransactions()

                    is UiEvent.UpdateAutoStartNwcService -> updateAutoStart(it.enabled)

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
                    }.onSuccess {
                        runCatching { pushNotificationsTokenUpdater.updateTokenForNwcService() }
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
            var lastFetchedWalletId: String? = null
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .distinctUntilChanged()
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

                    if (wallet?.walletId != lastFetchedWalletId) {
                        lastFetchedWalletId = wallet?.walletId
                        connectionsJob?.cancel()
                        connectionsJob = when (wallet) {
                            is Wallet.Spark -> observeSparkConnections()
                            is Wallet.Primal -> fetchPrimalWalletConnections(wallet)
                            else -> null
                        }
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeAutoStartState() =
        viewModelScope.launch {
            activeAccountStore.activeUserId
                .flatMapLatest { userId -> nwcRepository.observeAutoStartEnabled(userId) }
                .distinctUntilChanged()
                .collect { isEnabled ->
                    setState { copy(isAutoStartEnabled = isEnabled) }
                }
        }

    private fun updateAutoStart(enabled: Boolean) =
        viewModelScope.launch {
            runCatching {
                nwcRepository.updateAutoStartForUser(userId = activeAccountStore.activeUserId(), autoStart = enabled)
            }.onSuccess {
                runCatching { pushNotificationsTokenUpdater.updateTokenForNwcService() }
            }.onFailure {
                Napier.w(throwable = it) { "Failed to update auto-start setting." }
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
                if (state.value.activeWallet is Wallet.Spark) {
                    migratePrimalTransactionsHandler.invoke(
                        userId = activeAccountStore.activeUserId(),
                        targetSparkWalletId = activeWalletId,
                    ).getOrThrow()
                }
                walletRepository.ensureAllTransactionsSynced(walletId = activeWalletId)
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
}
