package net.primal.android.nostrconnect

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import net.primal.android.core.errors.UiError
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.domain.account.model.TrustLevel

interface NostrConnectContract {
    data class UiState(
        val appName: String?,
        val appWebUrl: String?,
        val appImageUrl: String?,
        val connectionUrl: String?,
        val accounts: List<UserAccountUi> = emptyList(),
        val selectedTab: Tab = Tab.LOGIN,
        val selectedAccount: UserAccountUi? = null,
        val trustLevel: TrustLevel = TrustLevel.Medium,
        val dailyBudget: Long? = null,
        val connecting: Boolean = false,
        val showDailyBudgetPicker: Boolean = false,
        val selectedDailyBudget: Long? = null,
        val budgetToUsdMap: Map<Long, BigDecimal?> = emptyMap(),
        val error: UiError? = null,
    )

    enum class Tab {
        LOGIN,
        PERMISSIONS,
    }

    sealed class UiEvent {
        data class ChangeTab(val tab: Tab) : UiEvent()
        data class SelectAccount(val pubkey: String) : UiEvent()
        data class SelectTrustLevel(val level: TrustLevel) : UiEvent()
        data object ClickDailyBudget : UiEvent()
        data class ChangeDailyBudget(val budget: Long?) : UiEvent()
        data object ApplyDailyBudget : UiEvent()
        data object CancelDailyBudget : UiEvent()
        data object ClickConnect : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object ConnectionSuccess : SideEffect()
    }

    companion object {
        val DAILY_BUDGET_OPTIONS = listOf(0L, 1000L, 5000L, 10_000L, 20_000L, 50_000L, 100_000L)
        val DAILY_BUDGET_PICKER_OPTIONS = DAILY_BUDGET_OPTIONS + listOf(null)
    }
}
