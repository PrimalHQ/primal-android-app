package net.primal.android.premium.legend.become

import java.math.BigDecimal
import net.primal.android.attachments.domain.CdnImage

class PremiumBecomeLegendContract {

    data class UiState(
        val isPremiumBadgeOrigin: Boolean = false,
        val stage: BecomeLegendStage = BecomeLegendStage.Intro,
        val avatarCdnImage: CdnImage? = null,
        val userHandle: String? = null,
        val isPremiumUser: Boolean = false,
        val primalName: String? = null,
        val isFetchingPaymentInstructions: Boolean = false,
        val minLegendThresholdInBtc: BigDecimal = BigDecimal.ZERO,
        val maxLegendThresholdInBtc: BigDecimal = BigDecimal.ONE,
        val exchangeBtcUsdRate: Double? = null,
        val selectedAmountInBtc: BigDecimal = minLegendThresholdInBtc,
        val bitcoinAddress: String? = null,
        val qrCodeValue: String? = null,
        val membershipQuoteId: String? = null,
    ) {
        fun arePaymentInstructionsAvailable() =
            this.minLegendThresholdInBtc != BigDecimal.ZERO &&
                this.selectedAmountInBtc != BigDecimal.ONE &&
                this.bitcoinAddress != null &&
                this.membershipQuoteId != null
    }

    sealed class UiEvent {
        data object ShowAmountEditor : UiEvent()
        data object GoBackToIntro : UiEvent()
        data object GoToFindPrimalNameStage : UiEvent()
        data class PrimalNamePicked(val primalName: String) : UiEvent()
        data object ShowPaymentInstructions : UiEvent()
        data class UpdateSelectedAmount(val newAmount: Float) : UiEvent()
        data object StartPurchaseMonitor : UiEvent()
        data object StopPurchaseMonitor : UiEvent()
        data object FetchPaymentInstructions : UiEvent()
    }

    enum class BecomeLegendStage {
        Intro,
        PickPrimalName,
        PickAmount,
        Payment,
        Success,
    }

    companion object {
        const val LEGEND_THRESHOLD_IN_USD = 1_000
    }
}
