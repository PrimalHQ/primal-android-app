package net.primal.android.core.compose.numericpad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.math.BigDecimal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.numericpad.PrimalNumericPadContract.SideEffect
import net.primal.android.core.compose.numericpad.PrimalNumericPadContract.State
import net.primal.android.core.compose.numericpad.PrimalNumericPadContract.UiEvent
import net.primal.android.core.compose.numericpad.PrimalNumericPadContract.UiEvent.NumericInputEvent

class PrimalNumericPadViewModel : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()
    private fun setState(reducer: State.() -> State) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _channel = Channel<SideEffect>()
    val channel = _channel.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _channel.send(effect) }

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is NumericInputEvent -> handleNumericPadInput(it)
                    is UiEvent.SetAmount -> setState { copy(amountInSats = it.valueInSats) }
                }
            }
        }

    private fun handleNumericPadInput(event: NumericInputEvent) {
        setState {
            val newValue = when (event) {
                NumericInputEvent.BackspaceEvent -> this.amountInSats.backspace()
                is NumericInputEvent.DigitInputEvent -> this.amountInSats.inputDigit(event.digit)
                NumericInputEvent.ResetAmountEvent -> "0"
            }
            copy(amountInSats = newValue)
        }
        setEffect(SideEffect.AmountChanged(amountInSats = _state.value.amountInSats))
    }

    private fun String.backspace(): String {
        return if (this.length > 1) {
            this.substring(0, this.length - 1)
        } else {
            "0"
        }
    }

    private fun String.inputDigit(digit: Int): String {
        val oldValue = this
        return if (oldValue.length < 8) {
            if (oldValue.isPositive()) {
                "$oldValue$digit"
            } else {
                "$digit"
            }
        } else {
            oldValue
        }
    }

    private fun String.isPositive() = toBigDecimal() > BigDecimal.ZERO
}
