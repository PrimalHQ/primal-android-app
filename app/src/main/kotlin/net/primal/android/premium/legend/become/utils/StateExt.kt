package net.primal.android.premium.legend.become.utils

import java.math.BigDecimal
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.UiState

fun UiState.arePaymentInstructionsAvailable() =
    this.minLegendThresholdInBtc != BigDecimal.ZERO &&
        this.selectedAmountInBtc != BigDecimal.ONE &&
        this.bitcoinAddress != null &&
        this.membershipQuoteId != null
