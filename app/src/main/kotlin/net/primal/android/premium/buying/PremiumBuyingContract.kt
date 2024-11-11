package net.primal.android.premium.buying

import android.app.Activity
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.premium.domain.MembershipError
import net.primal.android.wallet.store.domain.SubscriptionProduct

interface PremiumBuyingContract {
    data class UiState(
        val loading: Boolean = true,
        val isExtendingPremium: Boolean = false,
        val subscriptions: List<SubscriptionProduct> = emptyList(),
        val stage: PremiumStage = PremiumStage.Home,
        val primalName: String? = null,
        val hasActiveSubscription: Boolean = false,

        val profile: ProfileDetailsUi? = null,
        val promoCodeValidity: Boolean? = null,
        val isCheckingPromoCodeValidity: Boolean = false,

        val error: MembershipError? = null,
    )

    sealed class UiEvent {
        data class MoveToPremiumStage(val stage: PremiumStage) : UiEvent()
        data class SetPrimalName(val primalName: String) : UiEvent()

        data class ApplyPromoCode(val promoCode: String) : UiEvent()
        data object ClearPromoCodeValidity : UiEvent()

        data object RestoreSubscription : UiEvent()

        data object DismissError : UiEvent()

        data class RequestPurchase(
            val activity: Activity,
            val subscriptionProduct: SubscriptionProduct,
        ) : UiEvent()
    }

    enum class PremiumStage {
        Home,
        FindPrimalName,
        Purchase,
        Success,
    }
}
