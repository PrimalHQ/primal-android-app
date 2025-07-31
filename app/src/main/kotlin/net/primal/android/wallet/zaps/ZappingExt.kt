package net.primal.android.wallet.zaps

import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.user.domain.UserAccount
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.domain.wallet.WalletKycLevel

fun UserAccount.hasPrimalWallet(): Boolean = primalWallet != null && primalWallet.kycLevel != WalletKycLevel.None

fun ZappingState.canZap(zapAmount: Long = this.zapDefault.amount): Boolean {
    return walletConnected && (walletBalanceInBtc == null || walletBalanceInBtc.toSats() >= zapAmount.toULong())
}
