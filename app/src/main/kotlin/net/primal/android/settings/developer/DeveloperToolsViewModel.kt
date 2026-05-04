package net.primal.android.settings.developer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.logging.AppLogController
import net.primal.android.core.logging.AppLogExporter
import net.primal.android.core.logging.AppLogPreferences
import net.primal.android.core.logging.AppLogRecorder
import net.primal.android.settings.developer.DeveloperToolsContract.DevWalletInfo
import net.primal.android.settings.developer.DeveloperToolsContract.SideEffect
import net.primal.android.settings.developer.DeveloperToolsContract.UiEvent
import net.primal.android.settings.developer.DeveloperToolsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.WalletType

@Suppress("LongParameterList")
@HiltViewModel
class DeveloperToolsViewModel @Inject constructor(
    @ApplicationContext appContext: Context,
    private val dispatcherProvider: DispatcherProvider,
    private val logController: AppLogController,
    private val logRecorder: AppLogRecorder,
    private val logExporter: AppLogExporter,
    private val appLogPreferences: AppLogPreferences,
    private val activeAccountStore: ActiveAccountStore,
    private val walletAccountRepository: WalletAccountRepository,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val cacheDir: File by lazy { appContext.externalCacheDir ?: appContext.cacheDir }

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _uiState.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        refreshLogStats()
        observeWallets()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.ToggleLogging -> toggleLogging(event.enabled)
                    is UiEvent.ToggleWalletPicker -> toggleWalletPicker(event.enabled)
                    UiEvent.ExportLogs -> exportLogs()
                    UiEvent.ClearLogs -> clearLogs()
                    is UiEvent.CopySeedWords -> copySeedWords(event.walletId)
                    is UiEvent.DeleteWallet -> deleteWallet(event.walletId)
                }
            }
        }

    private fun refreshLogStats() {
        setState {
            copy(
                isLoggingEnabled = logController.loggingEnabled,
                isWalletPickerEnabled = appLogPreferences.walletPickerEnabled,
                logFileCount = logRecorder.getLogFileCount(),
                totalLogSizeBytes = logRecorder.getTotalLogSize(),
            )
        }
    }

    private fun toggleLogging(enabled: Boolean) {
        logController.setLoggingEnabled(enabled)
        refreshLogStats()
    }

    private fun toggleWalletPicker(enabled: Boolean) {
        appLogPreferences.setWalletPickerEnabled(enabled)
        setState { copy(isWalletPickerEnabled = enabled) }
    }

    private fun exportLogs() =
        viewModelScope.launch {
            setState { copy(isExporting = true) }
            runCatching {
                val zipFile = withContext(dispatcherProvider.io()) {
                    logExporter.exportLogsAsZip(cacheDir)
                }
                if (zipFile != null) {
                    setEffect(SideEffect.ShareLogs(zipFile))
                } else {
                    setEffect(SideEffect.NoLogsToExport)
                }
            }.onFailure { error ->
                setEffect(SideEffect.ExportFailed)
            }.onSuccess {
                delay(500.milliseconds)
            }
            setState { copy(isExporting = false) }
        }

    private fun clearLogs() =
        viewModelScope.launch {
            logRecorder.clearLogs()
            refreshLogStats()
        }

    private fun observeWallets() =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()

            combine(
                walletAccountRepository.observeWalletsByUser(userId),
                walletAccountRepository.observeActiveWallet(userId),
            ) { allWallets, activeUserWallet ->
                val activeWalletId = activeUserWallet?.wallet?.walletId

                allWallets
                    .filter { it.wallet.type == WalletType.SPARK || it.wallet.walletId == activeWalletId }
                    .map { userWallet ->
                        DevWalletInfo(
                            walletId = userWallet.wallet.walletId,
                            type = userWallet.wallet.type,
                            isActive = userWallet.wallet.walletId == activeWalletId,
                            lightningAddress = userWallet.lightningAddress,
                            balanceInSats = userWallet.wallet.balanceInBtc?.toSats()?.toLong(),
                        )
                    }
            }.collect { wallets ->
                setState { copy(wallets = wallets) }
            }
        }

    private fun copySeedWords(walletId: String) =
        viewModelScope.launch {
            sparkWalletAccountRepository.getPersistedSeedWords(walletId)
                .onSuccess { words ->
                    setEffect(SideEffect.SeedWordsCopied(seedWords = words.joinToString(" ")))
                }
                .onFailure {
                    setEffect(SideEffect.SeedWordsCopyFailed)
                }
        }

    private fun deleteWallet(walletId: String) =
        viewModelScope.launch {
            runCatching {
                val userId = activeAccountStore.activeUserId()

                // 1. Unregister if this is the registered Spark wallet
                val registeredWalletId = sparkWalletAccountRepository.findRegisteredSparkWalletId(userId)
                if (registeredWalletId == walletId) {
                    sparkWalletAccountRepository.unregisterSparkWallet(userId, walletId)
                }

                // 2. Delete all local data
                walletRepository.deleteWalletById(walletId)

                // 3. Re-register first available Spark wallet if none registered
                val newRegisteredId = sparkWalletAccountRepository.findRegisteredSparkWalletId(userId)
                if (newRegisteredId == null) {
                    val remainingSparkIds = sparkWalletAccountRepository.findAllPersistedWalletIds(userId)
                    val firstSparkId = remainingSparkIds.firstOrNull()
                    if (firstSparkId != null) {
                        sparkWalletAccountRepository.registerSparkWallet(userId, firstSparkId)
                        sparkWalletAccountRepository.fetchWalletAccountInfo(userId, firstSparkId)
                    }
                }

                // 4. Handle active wallet (independent from registration)
                val activeWallet = walletAccountRepository.observeActiveWallet(userId).first()
                if (activeWallet == null || activeWallet.wallet.walletId == walletId) {
                    val remainingSparkIds = sparkWalletAccountRepository.findAllPersistedWalletIds(userId)
                    val newActiveId = remainingSparkIds.firstOrNull()
                        ?: walletAccountRepository.observeWalletsByUser(userId).first().firstOrNull()?.wallet?.walletId

                    if (newActiveId != null) {
                        walletAccountRepository.setActiveWallet(userId, newActiveId)
                    }
                }

                setEffect(SideEffect.WalletDeleted)
            }.onFailure {
                setEffect(SideEffect.WalletDeleteFailed)
            }
        }
}
