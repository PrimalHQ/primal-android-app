package net.primal.android.settings.keys

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.LegendaryCustomization

interface KeysSettingsContract {
    data class UiState(
        val avatarCdnImage: CdnImage? = null,
        val nsec: String = "",
        val npub: String = "",
        val legendaryCustomization: LegendaryCustomization? = null,
    )
}
