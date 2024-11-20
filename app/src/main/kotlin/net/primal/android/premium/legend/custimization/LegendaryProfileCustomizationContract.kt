package net.primal.android.premium.legend.custimization

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.premium.legend.LegendaryStyle

interface LegendaryProfileCustomizationContract {

    data class UiState(
        val avatarCdnImage: CdnImage? = null,
        val membership: PremiumMembership? = null,
        val customBadge: Boolean = false,
        val avatarGlow: Boolean = false,
        val legendaryStyle: LegendaryStyle = LegendaryStyle.NO_CUSTOMIZATION,
        val applyingChanges: Boolean = false,
    )

    sealed class UiEvent {
        data class ApplyCustomization(
            val customBadge: Boolean,
            val avatarGlow: Boolean,
            val style: LegendaryStyle,
        ) : UiEvent()
    }

    sealed class SideEffect {
        data object CustomizationSaved : SideEffect()
    }
}
