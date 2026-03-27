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
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
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
            val sparkWalletIds = withContext(dispatcherProvider.io()) {
                sparkWalletAccountRepository.findAllPersistedWalletIds(userId)
            }
            val balanceByWalletId = walletAccountRepository.observeWalletsByUser(userId)
                .first()
                .associate { it.wallet.walletId to it.wallet.balanceInBtc }

            walletAccountRepository.observeActiveWallet(userId)
                .collect { activeUserWallet ->
                    val activeWallet = activeUserWallet?.wallet
                    val wallets = buildList {
                        if (activeUserWallet != null && activeWallet != null) {
                            add(
                                DevWalletInfo(
                                    walletId = activeWallet.walletId,
                                    type = activeWallet.type,
                                    isActive = true,
                                    lightningAddress = activeUserWallet.lightningAddress,
                                    balanceInSats = activeWallet.balanceInBtc?.toSats()?.toLong(),
                                ),
                            )
                        }
                        sparkWalletIds
                            .filter { it != activeWallet?.walletId }
                            .forEach { walletId ->
                                val address = withContext(dispatcherProvider.io()) {
                                    sparkWalletAccountRepository.getLightningAddress(userId, walletId)
                                }
                                add(
                                    DevWalletInfo(
                                        walletId = walletId,
                                        type = WalletType.SPARK,
                                        isActive = false,
                                        lightningAddress = address,
                                        balanceInSats = balanceByWalletId[walletId]?.toSats()?.toLong(),
                                    ),
                                )
                            }
                    }
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
}
