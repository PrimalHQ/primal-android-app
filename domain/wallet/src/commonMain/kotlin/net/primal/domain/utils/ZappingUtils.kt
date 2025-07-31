package net.primal.domain.utils

import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.domain.zaps.ZappingState

fun ZappingState.canZap(zapAmount: Long = this.zapDefault.amount): Boolean {
    return walletConnected && (walletBalanceInBtc == null || (walletBalanceInBtc).toSats() >= zapAmount.toULong())
}
