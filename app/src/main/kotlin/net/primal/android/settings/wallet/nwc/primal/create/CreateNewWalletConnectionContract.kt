package net.primal.android.settings.wallet.nwc.primal.create

import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.android.settings.wallet.nwc.primal.PrimalNwcDefaults

interface CreateNewWalletConnectionContract {
    data class UiState(
        val creatingSecret: Boolean = false,
        val appName: String = "",
        val nwcConnectionUri: String? = null,
        val dailyBudget: Long? = PrimalNwcDefaults.DEFAULT_DAILY_BUDGET,
        val activeAccount: UserAccountUi? = null,
    )

    sealed class UiEvent {
        data class AppNameChanged(val appName: String) : UiEvent()
        data class DailyBudgetChanged(val dailyBudget: Long?) : UiEvent()
        data object CreateWalletConnection : UiEvent()
    }

    sealed class SideEffect {
        data class CreateSuccess(val nwcServiceIsRequired: Boolean) : SideEffect()
    }
}
