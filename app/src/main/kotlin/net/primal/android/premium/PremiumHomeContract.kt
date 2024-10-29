package net.primal.android.premium

interface PremiumHomeContract {
    data class UiState(
        val stage: PremiumStage = PremiumStage.Home,
    )

    sealed class UiEvent {
        data class MoveToPremiumStage(val stage: PremiumStage) : UiEvent()
    }

    enum class PremiumStage {
        Home,
        FindPrimalName,
        Purchase,
    }
}
