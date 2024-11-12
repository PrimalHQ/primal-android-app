package net.primal.android.premium.legend

import java.math.BigDecimal
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.domain.PremiumMembership

class PremiumBecomeLegendContract {

    data class UiState(
        val stage: BecomeLegendStage = BecomeLegendStage.Intro,
        val displayName: String = "",
        val avatarCdnImage: CdnImage? = null,
        val profileNostrAddress: String? = null,
        val profileLightningAddress: String? = null,
        val membership: PremiumMembership? = null,
        val minLegendThresholdInBtc: BigDecimal = BigDecimal.ZERO,
        val maxLegendThresholdInBtc: BigDecimal = BigDecimal.ONE,
        val exchangeBtcUsdRate: Double? = null,
        val selectedAmountInBtc: BigDecimal = minLegendThresholdInBtc,
        val bitcoinAddress: String? = null,
        val qrCodeValue: String? = null,
    )

    sealed class UiEvent {
        data object ShowAmountEditor : UiEvent()
        data object GoBackToIntro : UiEvent()
        data object ShowPaymentInstructions : UiEvent()
        data object ShowSuccess : UiEvent()
        data class UpdateSelectedAmount(val newAmount: Float) : UiEvent()
    }

    enum class BecomeLegendStage {
        Intro,
        PickAmount,
        Payment,
        Success,
    }

    companion object {
        const val LEGEND_THRESHOLD_IN_USD = 1_000
    }
}
