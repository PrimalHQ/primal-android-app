package net.primal.android.premium.buying.purchase

import android.app.Activity
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.wallet.store.domain.SubscriptionProduct

interface PremiumPurchaseContract {
    data class UiState(
        val profile: ProfileDetailsUi? = null,
        val promoCodeValidity: Boolean? = null,
        val isCheckingPromoCodeValidity: Boolean = false,
    )

    sealed class UiEvent {
        data class ApplyPromoCode(val promoCode: String) : UiEvent()
        data object ClearPromoCodeValidity : UiEvent()
        data class RequestPurchase(
            val activity: Activity,
            val subscriptionProduct: SubscriptionProduct,
        ) : UiEvent()
    }
}
