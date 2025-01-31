package net.primal.android.settings.wallet.nwc.primal.link

import net.primal.android.settings.wallet.nwc.primal.PrimalNwcDefaults

interface LinkPrimalWalletContract {
    data class UiState(
        val creatingSecret: Boolean = false,
        val nwcConnectionUri: String? = null,
        val dailyBudget: Long? = PrimalNwcDefaults.DEFAULT_DAILY_BUDGET,
        val appName: String? = null,
        val appIcon: String? = null,
        val callback: String = "",
    )

    sealed class UiEvent {
        data class DailyBudgetChanged(val dailyBudget: Long?) : UiEvent()
        data object CreateWalletConnection : UiEvent()
    }

    sealed class SideEffect {
        data class UriReceived(val callbackUri: String) : SideEffect()
    }
}
