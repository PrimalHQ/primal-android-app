package net.primal.android.core.compose.numericpad

interface PrimalNumericPadContract {
    data class State(
        val amountInSats: String = "0",
    )

    sealed class UiEvent {
        sealed class NumericInputEvent : UiEvent() {
            data class DigitInputEvent(val digit: Int) : NumericInputEvent()

            data object DotInputEvent : NumericInputEvent()
            data object BackspaceEvent : NumericInputEvent()
            data object ResetAmountEvent : NumericInputEvent()
        }
        data class SetAmount(val valueInSats: String) : UiEvent()
    }

    sealed class SideEffect {
        data class AmountChanged(val amountInSats: String) : SideEffect()
    }
}
