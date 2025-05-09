package net.primal.android.redeem

import net.primal.android.core.errors.UiError

interface RedeemCodeContract {
    data class UiState(
        val userState: UserState = UserState.NoUser,
        val requiresPrimalWallet: Boolean = false,
        val promoCode: String? = null,
        val welcomeMessage: String? = null,
        val loading: Boolean = false,
        val showErrorBadge: Boolean = false,
        val error: UiError? = null,
        val stage: RedeemCodeStage = RedeemCodeStage.EnterCode,
        val promoCodeBenefits: List<PromoCodeBenefit> = emptyList(),
    )

    sealed class UiEvent {
        data object DismissError : UiEvent()
        data object GoToEnterCodeStage : UiEvent()
        data class GetCodeDetails(val code: String) : UiEvent()
        data class ApplyCode(val code: String) : UiEvent()
    }

    sealed class SideEffect {
        data object PromoCodeApplied : SideEffect()
    }

    enum class RedeemCodeStage {
        EnterCode,
        Success,
    }

    enum class UserState {
        NoUser,
        UserWithPrimalWallet,
        UserWithoutPrimalWallet,
    }

    sealed class PromoCodeBenefit {
        data class PrimalPremium(val durationInMonths: Int) : PromoCodeBenefit()
        data class WalletBalance(val sats: Double) : PromoCodeBenefit()
    }
}
