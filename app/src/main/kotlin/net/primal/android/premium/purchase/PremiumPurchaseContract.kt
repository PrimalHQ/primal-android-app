package net.primal.android.premium.purchase

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface PremiumPurchaseContract {
    data class UiState(
        val profile: ProfileDetailsUi? = null,
        val promoCodeValidity: Boolean? = null,
    )

    sealed class UiEvent {
        data class ApplyPromoCode(val promoCode: String) : UiEvent()
        data object ClearPromoCodeValidity : UiEvent()
    }
}
