package net.primal.android.settings.wallet.link

interface LinkPrimalWalletContract {
    companion object {
        const val DEFAULT_APP_NAME: String = "External app"
    }

    data class UiState(
        val creatingSecret: Boolean = false,
        val nwcConnectionUri: String? = null,
        val dailyBudget: Long? = 10_000,
        val appName: String? = null,
        val appIcon: String? = null,
        val callback: String = "",
    )

    sealed class UiEvent {
        data class DailyBudgetChanged(val dailyBudget: Long?) : UiEvent()
        data object CreateWalletConnection : UiEvent()
    }

    sealed class SideEffect {
        data class UriReceived(val nwcConnectionUri: String?) : SideEffect()
    }
}
