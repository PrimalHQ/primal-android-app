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
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.logging.AppLogController
import net.primal.android.core.logging.AppLogExporter
import net.primal.android.core.logging.AppLogRecorder
import net.primal.android.settings.developer.DeveloperToolsContract.SideEffect
import net.primal.android.settings.developer.DeveloperToolsContract.UiEvent
import net.primal.android.settings.developer.DeveloperToolsContract.UiState
import net.primal.core.utils.coroutines.DispatcherProvider

@HiltViewModel
class DeveloperToolsViewModel @Inject constructor(
    @ApplicationContext appContext: Context,
    private val dispatcherProvider: DispatcherProvider,
    private val logController: AppLogController,
    private val logRecorder: AppLogRecorder,
    private val logExporter: AppLogExporter,
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
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.ToggleLogging -> toggleLogging(event.enabled)
                    UiEvent.ExportLogs -> exportLogs()
                    UiEvent.ClearLogs -> clearLogs()
                }
            }
        }

    private fun refreshLogStats() {
        setState {
            copy(
                isLoggingEnabled = logController.loggingEnabled,
                logFileCount = logRecorder.getLogFileCount(),
                totalLogSizeBytes = logRecorder.getTotalLogSize(),
            )
        }
    }

    private fun toggleLogging(enabled: Boolean) {
        logController.setLoggingEnabled(enabled)
        refreshLogStats()
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
}
