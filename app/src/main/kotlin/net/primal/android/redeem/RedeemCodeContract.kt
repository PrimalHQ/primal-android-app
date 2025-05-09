package net.primal.android.redeem

import net.primal.android.core.errors.UiError
import net.primal.android.scanner.domain.QrCodeResult

interface RedeemCodeContract {
    data class UiState(
        val userState: UserState = UserState.NoUser,
        val requiresPrimalWallet: Boolean = false,
        val promoCode: String? = null,
        val welcomeMessage: String? = null,
        val loading: Boolean = false,
        val showErrorBadge: Boolean = false,
        val error: UiError? = null,
        val stageStack: List<RedeemCodeStage> = listOf(
            RedeemCodeStage.ScanCode,
        ),
        val promoCodeBenefits: List<PromoCodeBenefit> = emptyList(),
    ) {
        fun getStage() = stageStack.last()
    }

    sealed class UiEvent {
        data object DismissError : UiEvent()
        data object PreviousStage : UiEvent()
        data object GoToEnterCodeStage : UiEvent()
        data class QrCodeDetected(val result: QrCodeResult) : UiEvent()
        data class GetCodeDetails(val code: String) : UiEvent()
        data class ApplyCode(val code: String) : UiEvent()
    }

    sealed class SideEffect {
        data object PromoCodeApplied : SideEffect()
    }

    enum class RedeemCodeStage {
        ScanCode,
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
