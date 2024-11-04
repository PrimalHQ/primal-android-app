package net.primal.android.premium.home

interface PremiumHomeContract {
    data class UiState(
        val stage: PremiumStage = PremiumStage.Home,
        val primalName: String? = null,
    )

    sealed class UiEvent {
        data class MoveToPremiumStage(val stage: PremiumStage) : UiEvent()
        data class SetPrimalName(val primalName: String) : UiEvent()
    }

    enum class PremiumStage {
        Home,
        FindPrimalName,
        Purchase,
    }
}
