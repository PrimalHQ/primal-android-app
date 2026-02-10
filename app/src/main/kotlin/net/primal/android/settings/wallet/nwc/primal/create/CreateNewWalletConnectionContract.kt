package net.primal.android.settings.wallet.nwc.primal.create

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.settings.wallet.nwc.primal.PrimalNwcDefaults
import net.primal.domain.links.CdnImage

interface CreateNewWalletConnectionContract {
    data class UiState(
        val creatingSecret: Boolean = false,
        val appName: String = "",
        val nwcConnectionUri: String? = null,
        val dailyBudget: Long? = PrimalNwcDefaults.DEFAULT_DAILY_BUDGET,
        val activeUserId: String = "",
        val isServiceRunningForCurrentUser: Boolean = false,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val activeAccountDisplayName: String = "",
    )

    sealed class UiEvent {
        data class AppNameChanged(val appName: String) : UiEvent()
        data class DailyBudgetChanged(val dailyBudget: Long?) : UiEvent()
        data object CreateWalletConnection : UiEvent()
    }
}
