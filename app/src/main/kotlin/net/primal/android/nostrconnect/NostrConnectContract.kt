package net.primal.android.nostrconnect

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import net.primal.android.drawer.multiaccount.model.UserAccountUi

interface NostrConnectContract {
    data class UiState(
        val appName: String?,
        val appUrl: String?,
        val appImageUrl: String?,
        val accounts: List<UserAccountUi> = emptyList(),
        val selectedTab: Tab = Tab.LOGIN,
        val selectedAccount: UserAccountUi? = null,
        val trustLevel: TrustLevel = TrustLevel.MEDIUM,
        val dailyBudget: Long? = null,
        val connecting: Boolean = false,
        val showDailyBudgetPicker: Boolean = false,
        val selectedDailyBudget: Long? = null,
        val budgetToUsdMap: Map<Long, BigDecimal?> = emptyMap(),
    )

    enum class Tab {
        LOGIN,
        PERMISSIONS,
    }

    enum class TrustLevel {
        LOW,
        MEDIUM,
        FULL,
    }

    sealed class UiEvent {
        data class TabChanged(val tab: Tab) : UiEvent()
        data class AccountSelected(val pubkey: String) : UiEvent()
        data class TrustLevelSelected(val level: TrustLevel) : UiEvent()
        data object DailyBudgetClicked : UiEvent()
        data class DailyBudgetChanged(val budget: Long?) : UiEvent()
        data object DailyBudgetApplied : UiEvent()
        data object DailyBudgetCancelled : UiEvent()
        data object ConnectClicked : UiEvent()
        data object CancelClicked : UiEvent()
    }

    sealed class SideEffect {
        data object ConnectionSuccess : SideEffect()
        data class ConnectionFailed(val error: Throwable) : SideEffect()
    }

    companion object {
        val DAILY_BUDGET_OPTIONS = listOf(0L, 1000L, 5000L, 10_000L, 20_000L, 50_000L, 100_000L)
        val DAILY_BUDGET_PICKER_OPTIONS = DAILY_BUDGET_OPTIONS + listOf(null)
    }
}
