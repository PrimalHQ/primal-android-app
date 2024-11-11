package net.primal.android.premium.legend

class PremiumBecomeLegendContract {

    data class UiState(
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data object ShowAmountEditor : UiEvent()
    }
}
