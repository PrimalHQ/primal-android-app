package net.primal.android.settings.wallet.connection

interface NwcNewWalletConnectionContract {
    companion object {
        val budgetOptions = listOf("1000", "10000", "100000", "1000000", "no limit")
    }

    data class UiState(
        val creatingSecret: Boolean = false,
        val appName: String = "",
        val secret: String? = null,
        val nwcConnectionUri: String? = null,
        val dailyBudget: String? = "10000",
    )

    sealed class UiEvent {
        data class AppNameChanged(val appName: String) : UiEvent()
        data class DailyBudgetChanged(val dailyBudget: String?) : UiEvent()
        data object CreateWalletConnection : UiEvent()
    }

    sealed class SideEffect
}
