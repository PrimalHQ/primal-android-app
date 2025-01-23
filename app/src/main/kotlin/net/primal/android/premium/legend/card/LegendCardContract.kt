package net.primal.android.premium.legend.card

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface LegendCardContract {
    data class UiState(
        val profile: ProfileDetailsUi? = null,
    )
}
