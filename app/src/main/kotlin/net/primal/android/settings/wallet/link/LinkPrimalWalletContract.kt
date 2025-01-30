package net.primal.android.settings.wallet.link

interface LinkPrimalWalletContract {
    companion object {
        val budgetOptions: List<Long?> = listOf(1000, 10_000, 100_000, 1_000_000, null)
    }

    data class UiState(
        val creatingSecret: Boolean = false,
        val secret: String? = null,
        val dailyBudget: Long? = 10_000,
        val appName: String? = null,
        val appIcon: String? = null,
        val callback: String = "",
    )

    sealed class UiEvent {
        data class DailyBudgetChanged(val dailyBudget: Long?) : UiEvent()
    }
}
