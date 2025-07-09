package net.primal.android.premium.card

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface PremiumCardContract {
    data class UiState(
        val profile: ProfileDetailsUi? = null,
        val isActiveAccountCard: Boolean = false,
        val isActiveAccountLegend: Boolean = false,
        val isPrimalLegend: Boolean = false,
    )

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onSeeOtherLegendsClick: () -> Unit,
        val onSeeOtherPrimalOGsClick: () -> Unit,
        val onBecomeLegendClick: () -> Unit,
        val onLegendSettingsClick: () -> Unit,
    )
}
