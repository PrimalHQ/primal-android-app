package net.primal.android.drawer

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.user.domain.Badges
import net.primal.android.user.domain.UserAccount
import net.primal.domain.profile.Nip05VerificationStatus

interface PrimalDrawerContract {

    data class UiState(
        val menuItems: List<DrawerScreenDestination>,
        val loading: Boolean = false,
        val activeUserAccount: UserAccount? = null,
        val badges: Badges = Badges(),
        val showPremiumBadge: Boolean = false,
        val legendaryCustomization: LegendaryCustomization? = null,
        val activeUserNip05Status: Nip05VerificationStatus? = null,
        val themeManuallyInvertedTimestamp: Long? = null,
    )

    sealed class UiEvent {
        data class ThemeSwitchClick(val isSystemInDarkTheme: Boolean) : UiEvent()
    }
}
