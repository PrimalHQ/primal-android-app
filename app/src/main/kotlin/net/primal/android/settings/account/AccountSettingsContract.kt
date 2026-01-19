package net.primal.android.settings.account

import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage

interface AccountSettingsContract {
    data class UiState(
        val avatarCdnImage: CdnImage? = null,
        val nsec: String? = null,
        val npub: String = "",
        val legendaryCustomization: LegendaryCustomization? = null,
    )
}
