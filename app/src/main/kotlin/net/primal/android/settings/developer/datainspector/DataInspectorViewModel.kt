package net.primal.android.settings.developer.datainspector

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.settings.developer.datainspector.DataInspectorContract.SideEffect
import net.primal.android.settings.developer.datainspector.DataInspectorContract.UiEvent
import net.primal.android.settings.developer.datainspector.DataInspectorContract.UiState
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching

@HiltViewModel
class DataInspectorViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val exportStagingDir: File by lazy {
        File(appContext.externalCacheDir ?: appContext.cacheDir, "data_exports")
    }

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
        loadFiles()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.ExportFile -> exportFile(event.absolutePath)
                }
            }
        }

    private fun loadFiles() =
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val dataRoot = appContext.filesDir.parentFile
            val files = if (dataRoot == null) {
                emptyList()
            } else {
                withContext(dispatcherProvider.io()) {
                    collectDataFiles(
                        dataRoot = dataRoot,
                        excludedDirs = listOfNotNull(appContext.cacheDir, appContext.codeCacheDir),
                    )
                }
            }
            setState {
                copy(
                    isLoading = false,
                    files = files,
                    totalSizeBytes = files.sumOf { it.sizeBytes },
                )
            }
        }

    private fun exportFile(absolutePath: String) =
        viewModelScope.launch {
            val source = File(absolutePath)
            if (!source.exists()) {
                setEffect(SideEffect.ExportFailed)
                return@launch
            }
            runCatching {
                withContext(dispatcherProvider.io()) {
                    if (exportStagingDir.exists()) exportStagingDir.deleteRecursively()
                    exportStagingDir.mkdirs()
                    val target = File(exportStagingDir, source.name)
                    source.copyTo(target = target)
                    target
                }
            }.onSuccess { staged ->
                setEffect(SideEffect.ShareFile(staged))
            }.onFailure {
                setEffect(SideEffect.ExportFailed)
            }
        }
}
