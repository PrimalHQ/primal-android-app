package net.primal.android.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.scan.ScanContract.SideEffect
import net.primal.android.scan.ScanContract.UiEvent
import net.primal.android.scan.ScanContract.UiState
import net.primal.android.scan.analysis.QrCodeResult

@HiltViewModel
class ScanViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            _event.collect {
                processEvent(it)
            }
        }
    }

    private fun processEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ProcessScannedData -> processScannedData(result = event.result)
        }
    }

    private fun processScannedData(result: QrCodeResult) =
        viewModelScope.launch {
            when (result.type) {
                else -> setEffect(SideEffect.ScanningCompleted)
            }
        }
}
