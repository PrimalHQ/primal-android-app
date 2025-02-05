package net.primal.android.premium.legend.customization

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle

interface LegendaryProfileCustomizationContract {

    data class UiState(
        val avatarCdnImage: CdnImage? = null,
        val membership: PremiumMembership? = null,
        val avatarLegendaryCustomization: LegendaryCustomization = LegendaryCustomization(),
    ) {
        fun computeShoutout() = membership?.editedShoutout ?: avatarLegendaryCustomization.currentShoutout ?: ""
    }

    sealed class UiEvent {
        data class ApplyCustomization(
            val customBadge: Boolean? = null,
            val avatarGlow: Boolean? = null,
            val inLeaderboard: Boolean? = null,
            val style: LegendaryStyle? = null,
            val editedShoutout: String? = null,
        ) : UiEvent()
    }
}
