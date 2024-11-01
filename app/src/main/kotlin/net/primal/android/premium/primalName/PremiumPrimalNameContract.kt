package net.primal.android.premium.primalName

interface PremiumPrimalNameContract {

    data class UiState(
        val isNameAvailable: Boolean? = null,
    )

    sealed class UiEvent {
        data class CheckPrimalName(val name: String) : UiEvent()
        data object ResetNameAvailable : UiEvent()
    }
}