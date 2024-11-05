package net.primal.android.premium.purchase

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

interface PremiumPurchaseContract {
    data class UiState(
        val profile: ProfileDetailsUi? = null,
        val promoCodeValidity: Boolean? = null,
        val isCheckingPromoCodeValidity: Boolean = false,
    )

    sealed class UiEvent {
        data class ApplyPromoCode(val promoCode: String) : UiEvent()
        data object ClearPromoCodeValidity : UiEvent()
    }
}
