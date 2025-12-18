package net.primal.android.core.compose.signer.model

import net.primal.android.core.errors.UiError
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.domain.account.model.TrustLevel

data class SignerConnectUiState(
    val appName: String?,
    val appDescription: String?,
    val appImageUrl: String?,
    val connectionUrl: String?,
    val accounts: List<UserAccountUi> = emptyList(),
    val selectedTab: SignerConnectTab = SignerConnectTab.Login,
    val selectedAccount: UserAccountUi? = null,
    val trustLevel: TrustLevel = TrustLevel.Medium,
    val connecting: Boolean = false,
    val error: UiError? = null,
    // val dailyBudget: Long? = null,
    // val showDailyBudgetPicker: Boolean = false,
    // val selectedDailyBudget: Long? = null,
    // val budgetToUsdMap: Map<Long, BigDecimal?> = emptyMap(),
)

enum class SignerConnectTab {
    Login,
    Permissions,
}

/*
companion object {
    val DAILY_BUDGET_OPTIONS = listOf(0L, 1000L, 5000L, 10_000L, 20_000L, 50_000L, 100_000L)
    val DAILY_BUDGET_PICKER_OPTIONS = DAILY_BUDGET_OPTIONS + listOf(null)
}
 */
