package net.primal.android.nostrconnect

import net.primal.android.core.errors.UiError
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.domain.account.model.TrustLevel

interface NostrConnectContract {
    data class UiState(
        val appName: String?,
        val appDescription: String?,
        val appImageUrl: String?,
        val connectionUrl: String?,
        val accounts: List<UserAccountUi> = emptyList(),
        val connecting: Boolean = false,
        val error: UiError? = null,
        // val dailyBudget: Long? = null,
        // val showDailyBudgetPicker: Boolean = false,
        // val selectedDailyBudget: Long? = null,
        // val budgetToUsdMap: Map<Long, BigDecimal?> = emptyMap(),
    )

    sealed class UiEvent {
        data class ClickConnect(val account: UserAccountUi, val trustLevel: TrustLevel) : UiEvent()
        data object DismissError : UiEvent()
        /*
        data object ClickDailyBudget : UiEvent()
        data class ChangeDailyBudget(val budget: Long?) : UiEvent()
        data object ApplyDailyBudget : UiEvent()
        data object CancelDailyBudget : UiEvent()
         */
    }

    sealed class SideEffect {
        data object ConnectionSuccess : SideEffect()
    }
}
