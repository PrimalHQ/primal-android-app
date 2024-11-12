package net.primal.android.premium.legend

class PremiumBecomeLegendContract {

    data class UiState(
        val stage: BecomeLegendStage = BecomeLegendStage.Intro,
    )

    sealed class UiEvent {
        data object ShowAmountEditor : UiEvent()
        data object GoBackToIntro : UiEvent()
        data object ShowPaymentInstructions : UiEvent()
        data object ShowSuccess : UiEvent()
    }

    enum class BecomeLegendStage {
        Intro,
        PickAmount,
        Payment,
        Success,
    }
}
