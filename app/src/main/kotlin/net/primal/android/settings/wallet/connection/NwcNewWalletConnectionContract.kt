package net.primal.android.settings.wallet.connection

interface NwcNewWalletConnectionContract {
    data class UiState(
        val creatingSecret: Boolean = false,
        val appName: String = "",
        val secret: String? = null,
        val nwcConnectionUri: String? = null,
        val dailyBudget: Long? = 10_000,
    )

    sealed class UiEvent {
        data class AppNameChanged(val appName: String) : UiEvent()
        data class DailyBudgetChanged(val dailyBudget: Long?) : UiEvent()
        data object CreateWalletConnection : UiEvent()
    }

    sealed class SideEffect
}
