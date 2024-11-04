package net.primal.android.premium.purchase

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface PremiumPurchaseContract {
    data class UiState(
        val profile: ProfileDetailsUi? = null,
    )

    sealed class UiEvent {
        data class ApplyPromoCode(val promoCode: String) : UiEvent()
    }

    sealed class SideEffect {
        data object InvalidPromoCode : SideEffect()
    }
}
